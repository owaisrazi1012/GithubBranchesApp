package com.nisum.service.impl;

import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.nisum.api.preset.domain.dto.EmailDetails;
import com.nisum.api.preset.domain.entity.JenkinsBuildInfo;
import com.nisum.api.preset.repository.JenkinsBuildInfoRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.elasticsearch.domain.BuildStatus;
import com.nisum.elasticsearch.domain.DetailExecutionReport;
import com.nisum.elasticsearch.service.ElasticSearchService;
import com.nisum.service.PostJenkinsActionService;
import com.nisum.util.GenericUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Profile({"dev", "qa"})
public class PostJenkinsActionsServiceImpl implements PostJenkinsActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostJenkinsActionsServiceImpl.class);

    @Autowired
    private TestPresetRepository testPresetRepository;

    @Autowired
    private JenkinsBuildInfoRepository jenkinsBuildInfoRepository;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public boolean saveJenkinsBuildInfo(BuildInfo buildInfo, String project, long buildDate, String attachment,
                                        Boolean isFailureScenarioExist) {
        try {
            if (buildInfo != null && buildDate != 0) {
                List<String> triggerDates = new ArrayList<>();
                triggerDates.add(GenericUtils.formatDateForELK(buildDate));
                String failedTests = "";
                Boolean isDataRead= false;
                while(isFailureScenarioExist && !isDataRead) {
                    failedTests =
                            elasticSearchService.getFailedScenariosWithSteps(project, triggerDates, buildInfo.id());
                    if(failedTests.isEmpty()) {
                        //wait for 0.5 sec for next retrieval call
                        LOGGER.info("waiting for failed steps retrieval");
                        Thread.sleep(500);
                    } else
                        isDataRead = Boolean.TRUE;
                }
                LOGGER.info("failedTests {} " , failedTests );
                LOGGER.info("Saving Email/Jira Content");
                jenkinsBuildInfoRepository
                        .save(JenkinsBuildInfo.builder()
                                .jobName(project)
                                .buildDate(buildDate)
                                .buildStatus(BuildStatus.valueOf(buildInfo.result()))
                                .buildNumber(buildInfo.id())
                                .failedTests(failedTests)
                                .isEmailSent(false)
                                .isJiraCreated(false)
                                .build());
                LOGGER.info("Email Content Saved!");
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred on saving Email Content ", e);
        }
        return false;
    }

    // To send a simple email
    public boolean sendEmail(EmailDetails emailDetails) {
        if (emailDetails != null) {
            // Creating a mime message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper;

            try {
                if (null != emailDetails.getAttachment() && !emailDetails.getAttachment().isEmpty()) {
                    LOGGER.info("Preparing Email with Attachment");
                    // Setting multipart as true for attachments to be sent
                    mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
                    // Adding the attachment
                    FileSystemResource file = new FileSystemResource(new File(emailDetails.getAttachment()));
                    mimeMessageHelper.addAttachment(file.getFilename(), file);
                } else {
                    LOGGER.info("Preparing Email without Attachment");
                    mimeMessageHelper = new MimeMessageHelper(mimeMessage, false);
                }

                // Setting up necessary details
                mimeMessageHelper.setFrom(sender);
                mimeMessageHelper.setTo(emailDetails.getRecipients().split(","));
                mimeMessageHelper.setText(emailDetails.getBody());
                mimeMessageHelper.setSubject(emailDetails.getSubject());

                LOGGER.info("Sending Email...");
                // Sending the mail
                javaMailSender.send(mimeMessage);
                return true;
            }

            // Catch block to handle the exceptions
            catch (Exception e) {
                LOGGER.error("Exception occurred while Sending Mail", e);
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }
        return false;
    }
}