package com.nisum.service;

import com.nisum.api.preset.domain.dto.EmailDetails;
import com.nisum.api.preset.domain.entity.JenkinsBuildInfo;
import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.api.preset.repository.JenkinsBuildInfoRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.elasticsearch.domain.BuildStatus;
import com.nisum.jira.domain.dto.Content;
import com.nisum.jira.domain.dto.Description;
import com.nisum.jira.domain.dto.Fields;
import com.nisum.jira.domain.dto.IssueType;
import com.nisum.jira.domain.dto.Priority;
import com.nisum.jira.domain.dto.Project;
import com.nisum.jira.domain.dto.TicketDetail;
import com.nisum.util.GenericUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Profile({"dev", "qa"})
public class EmailJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailJob.class);

    @Autowired
    private PostJenkinsActionService postJenkinsActionService;

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JenkinsBuildInfoRepository jenkinsBuildInfoRepository;

    @Autowired
    private TestPresetRepository testPresetRepository;

    @Autowired
    private SlackService slackService;

    @Autowired
    private  TeamsService teamsService;

    @Value("${email.subject}")
    private String emailSubject;
    @Value("${email.body}")
    private String emailBody;

    @Value("${detail-report-url}")
    private String detailReportURL;


    @Scheduled(cron = "0 */2 * ? * *")
    private void triggerJob() {
        try {
            LOGGER.info("Scheduler Triggered...");
            List<JenkinsBuildInfo> jenkinsBuildInfos = jenkinsBuildInfoRepository.findByIsEmailSentOrIsJiraCreated(false, false);
            if (jenkinsBuildInfos.isEmpty()) {
                LOGGER.info("No Pending Emails or Jira Tickets Found");
            }
            HashMap<Long, List<JenkinsBuildInfo>> builds = new HashMap<>();
            for (JenkinsBuildInfo jenkinsBuildInfo : jenkinsBuildInfos) {
                if (!builds.containsKey(jenkinsBuildInfo.getBuildDate())) {
                    List<JenkinsBuildInfo> jenkinsBuildInfoList = new ArrayList<>();
                    jenkinsBuildInfoList.add(jenkinsBuildInfo);
                    builds.put(jenkinsBuildInfo.getBuildDate(), jenkinsBuildInfoList);
                } else {
                    builds.get(jenkinsBuildInfo.getBuildDate()).add(jenkinsBuildInfo);
                }
            }

            for (Map.Entry<Long, List<JenkinsBuildInfo>> build : builds.entrySet()) {
                TicketDetail ticket = new TicketDetail();


                TestPreset testPreset = testPresetRepository.findByName(build.getValue().get(0).getJobName());
                if (testPreset != null) {
                    // Email Work
                    LOGGER.info("Fetching Notification Config for Test Preset: {}", testPreset.getName());
                    if (testPreset.getNotificationPreset() != null && !testPreset.getNotificationPreset().getRecipients().isEmpty()) {
                        if (!build.getValue().get(0).isEmailSent()) {
                            LOGGER.info("Email Recipients: {}", testPreset.getNotificationPreset().getRecipients());
                            EmailDetails emailDetails = new EmailDetails();
                            emailDetails.setRecipients(testPreset.getNotificationPreset().getRecipients());
                            emailDetails.setAttachment("");
                            emailDetails.setSubject(emailSubject);
                            emailDetails.setSubject(emailDetails.getSubject()
                                    .replace("$project", build.getValue().get(0).getJobName()));
                            emailDetails.setBody(emailBody);

                            emailDetails.setBody(emailDetails.getBody().replace("$url", detailReportURL));

                            emailDetails.setBody(emailDetails.getBody()
                                    .replace("$project", build.getValue().get(0).getJobName())
                                    .replace("$buildDate", GenericUtils.formatDateForELK(build.getKey())));
                            LOGGER.info("Email Subject: {}", emailDetails.getSubject());
                            boolean isEmailSent = postJenkinsActionService.sendEmail(emailDetails);
                            if (isEmailSent) {
                                slackService.sendMessageToSlack(emailDetails.getBody());
                                teamsService.sendMessageToTeams(emailDetails.getBody());
                                LOGGER.info("Sending slack notification ..");
                                this.updateEmailJiraFlagIfProcessed(build.getKey(), false, null);
                                LOGGER.info("Email Sent Successfully for Build Date: {}", build.getKey());
                            } else {
                                LOGGER.info("Email Sending Failed for Build Date: {}", build.getKey());
                            }
                        }
                    } else {
                        LOGGER.info("Emails are not defined for for Test Preset: {}", testPreset.getName());
                    }

                    // JIRA Work
                    JiraPreset jiraPreset = testPreset.getJiraPreset();
                    if (jiraPreset != null) {
                        LOGGER.info("Jira Preset Config: {}", jiraPreset);

                        Description description = new Description();
                        description.setType("doc");
                        description.setVersion(1);
                        description.setContent(new ArrayList<>());


                        for (JenkinsBuildInfo jenkinsBuildInfo : build.getValue()) {
                            if (jenkinsBuildInfo.getBuildStatus() != null && jenkinsBuildInfo.getBuildStatus().equals(BuildStatus.SUCCESS) && !jenkinsBuildInfo.getFailedTests().isEmpty()) {
                                description.getContent().add(this.mapTextToContent("Build # " + jenkinsBuildInfo.getBuildNumber()));
                                description.getContent().add(this.mapTextToContent(jenkinsBuildInfo.getFailedTests()));
                            }
                        }

                        if (!description.getContent().isEmpty()) {
                            Fields fields = new Fields();

                            // Default Issue Type [Bug]
                            fields.setIssuetype(new IssueType("1"));
                            // Default Priority [Medium]
                            fields.setPriority(new Priority("10001"));
                            // Project which is mentioned in Jira Preset Config
                            fields.setProject(new Project(jiraPreset.getProjectId()));

                            fields.setSummary(emailSubject);
                            fields.setSummary(fields.getSummary()
                                    .replace("$project", build.getValue().get(0).getJobName()));


                            fields.setDescription(description);

                            String ticketId = jiraService.getTicket(fields.getSummary(), jiraPreset,build.getValue().get(0).getJobName());

                            if (ticketId != null) {
                                ticket.setKey(ticketId);
                                ticket.setBody(fields.getDescription());
                                ticket = jiraService.addCommentToTicket(ticket, jiraPreset);
                            } else {
                                ticket.setFields(fields);
                                ticket = jiraService.createJiraTicket(ticket, jiraPreset);
                            }

                            boolean isJiraCreated = ticket != null && ticket.getErrorMessage() == null && ticket.getKey() != null;
                            if (isJiraCreated) {
                                this.updateEmailJiraFlagIfProcessed(build.getKey(), null, false);
                                LOGGER.info("Jira Created Successfully with Ticket Number: {}", ticket.getKey());
                            } else {
                                LOGGER.info("Jira Created Failed. Reason : {}", ticket.getErrorMessage());
                            }
                        } else {
                            LOGGER.info("Updating Jira Created flag with 'True' as all executed test scenarios were passed");
                            this.updateEmailJiraFlagIfProcessed(build.getKey(), null, false);
                            LOGGER.info("Jira Ticket is not needed for all successful executed test scenarios.");
                        }

                    } else {
                        LOGGER.info("Jira Preset is not defined for for Test Preset: {}", testPreset.getName());
                    }
                }

            }


            LOGGER.info("Deleting already processed jenkins job notifications...");
            for (JenkinsBuildInfo jenkinsBuildInfo : jenkinsBuildInfoRepository.findByIsEmailSentAndIsJiraCreated(true, true)) {
                jenkinsBuildInfoRepository.delete(jenkinsBuildInfo);
                LOGGER.info("Jenkins Job Processed Notification Delete with Id : {}", jenkinsBuildInfo.getId());
            }

        } catch (Exception e) {
            LOGGER.error("Failed to Trigger Scheduler... ", e);
        }
    }

    private void updateEmailJiraFlagIfProcessed(Long buildDate, Boolean isEmailSent, Boolean isJiraCreated) {
        if (isEmailSent != null && !isEmailSent) {
            for (JenkinsBuildInfo jenkinsBuildInfo : jenkinsBuildInfoRepository.findByBuildDateAndIsEmailSent(buildDate, isEmailSent)) {
                jenkinsBuildInfo.setEmailSent(true);
                jenkinsBuildInfoRepository.save(jenkinsBuildInfo);
            }
        }
        if (isJiraCreated != null && !isJiraCreated) {
            for (JenkinsBuildInfo jenkinsBuildInfo : jenkinsBuildInfoRepository.findByBuildDateAndAndIsJiraCreated(buildDate, isJiraCreated)) {
                jenkinsBuildInfo.setJiraCreated(true);
                jenkinsBuildInfoRepository.save(jenkinsBuildInfo);
            }
        }
    }

    private Content mapTextToContent(String text) {
        Content content = new Content();
        content.setType("paragraph");
        content.setContent(new ArrayList<>());
        content.getContent().add(new Content("text", null, text));
        return content;
    }

}