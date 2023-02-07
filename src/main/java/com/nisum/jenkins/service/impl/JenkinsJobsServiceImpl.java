package com.nisum.jenkins.service.impl;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.common.IntegerResponse;
import com.cdancy.jenkins.rest.domain.common.RequestStatus;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.cdancy.jenkins.rest.domain.job.JobList;
import com.cdancy.jenkins.rest.domain.job.ProgressiveText;
import com.nisum.api.preset.domain.entity.JenkinsPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.api.preset.service.TestingPresetService;
import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.exception.custom.NoRecordFoundException;
import com.nisum.exception.custom.NoResourceFoundException;
import com.nisum.jenkins.domain.Action;
import com.nisum.jenkins.domain.Cause;
import com.nisum.jenkins.domain.Error;
import com.nisum.jenkins.domain.JenkinsBuildInfo;
import com.nisum.jenkins.domain.JenkinsJob;
import com.nisum.jenkins.domain.JenkinsJobInput;
import com.nisum.jenkins.domain.Parameter;
import com.nisum.jenkins.domain.Response;
import com.nisum.jenkins.service.JenkinsJobXmlService;
import com.nisum.jenkins.service.JenkinsJobsService;
import com.nisum.util.GenericUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
@Profile({"dev", "qa"})
public class JenkinsJobsServiceImpl implements JenkinsJobsService {

    @Value("${jenkins.logs.params}")
    private String jenkinsLogsParams;

//    private JenkinsClient jenkinsClient;
    private JenkinsJobXmlService jenkinsJobXmlService;
    private TestingPresetService testingPresetService;


    @Autowired
    RestTemplate restTemplate;

//    public JenkinsJobsServiceImpl(JenkinsClient jenkinsClient, JenkinsJobXmlService jenkinsJobXmlService, TestingPresetService testingPresetService) {
    public JenkinsJobsServiceImpl(JenkinsJobXmlService jenkinsJobXmlService, TestingPresetService testingPresetService) {
//        this.jenkinsClient = jenkinsClient;
        this.jenkinsJobXmlService = jenkinsJobXmlService;
        this.testingPresetService = testingPresetService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsJobsServiceImpl.class);

    private static final String JENKINS_JOB_CONFIG_XML1 = "<?xml version='1.1' encoding='UTF-8'?><flow-definition plugin=\"workflow-job@2.40\"><actions><org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction plugin=\"pipeline-model-definition@1.7.2\"/><org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction plugin=\"pipeline-model-definition@1.7.2\"><jobProperties/><triggers/><parameters/><options/></org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction></actions>";

    public List<JenkinsJob> fetchAllJenkinsJobs() {
        return null;
//        JobList jobList = jenkinsClient.api().jobsApi().jobList("");
//        return Optional.ofNullable(jobList).map(JobList::jobs).map(Collection::stream)
//                .orElseGet(Stream::empty).map(job -> JenkinsJob.builder().jobName(job.name()).url(job.url()).build()).collect(Collectors.toList());
    }

    private JenkinsClient getJenkinsClient(String jenkinsHostUrl, String jenkinsUserName
            , String jenkinsPassword) {
        return JenkinsClient.builder()
                .endPoint(jenkinsHostUrl)
                .credentials(jenkinsUserName + ":" + jenkinsPassword)
                .build();
    }

    public Response createJenkinsJob(String jobName) {
        String configXml = jenkinsConfigurationXml(jobName);
        LOGGER.info("Job is creating. JobName : {}", jobName);
//        RequestStatus status = jenkinsClient.api().jobsApi().create(null, jobName, configXml);
        JenkinsPreset jenkinsPreset = testingPresetService.getJenkinsPresetByTestPresetName(jobName);
        RequestStatus status = getJenkinsClient(jenkinsPreset.getUrl(),
                jenkinsPreset.getUserName(), jenkinsPreset.getPassword()).api().jobsApi().
                create(null, jobName, configXml);

        Response response = Response.builder().build();

        if (status.value().booleanValue()) {
            response.setMessage("job has created successfully");
        } else {
            if (status.errors() != null) {
                response.setErrors(buildError(status.errors()));
            }
            response.setMessage("job already exists with the name");
        }
        return response;
    }

    private List<Error> buildError(List<com.cdancy.jenkins.rest.domain.common.Error> jenkinsErrors) {
        return Optional.of(jenkinsErrors).map(Collection::stream).orElseGet(Stream::empty).map(jenkinsError ->
                Error.builder().context(jenkinsError.context()).exceptionName(jenkinsError.exceptionName())
                        .build()
        ).collect(Collectors.toList());
    }

    public Response triggerJenkinsJob(JenkinsJobInput jenkinsJobInput) throws Exception {
        LOGGER.info("triggering jenkins job");
        jenkinsJobInput.setCredentialId(
                StringUtils.isBlank(jenkinsJobInput.getCredentialId()) ? "NA" : jenkinsJobInput.getCredentialId());

        validateInputData(jenkinsJobInput.getGitRepoUrl(), jenkinsJobInput.getGitUserName()
                , jenkinsJobInput.getBranch(), jenkinsJobInput.getCredentialId(), jenkinsJobInput.getJenkinsSlave()
                , jenkinsJobInput.getJenkinsUrl(), jenkinsJobInput.getJenkinsUserName(), jenkinsJobInput.getJenkinsPassword()
                , jenkinsJobInput.getPresetName(), jenkinsJobInput.getExecutionPresetConfig()
        );

        validateRepositoryUrl(jenkinsJobInput.getGitRepoUrl());

        if (CollectionUtils.isNotEmpty(jenkinsJobInput.getTagsLineNumbers())) {
            validateInputData(jenkinsJobInput.getTagsLineNumbers().toArray(new String[]{}));
        }

        jenkinsJobInput.setGitUserName(jenkinsJobInput.getGitUserName().trim());
        jenkinsJobInput.setGitRepoUrl(jenkinsJobInput.getGitRepoUrl().trim());
        jenkinsJobInput.setCredentialId(jenkinsJobInput.getCredentialId().trim());
        jenkinsJobInput.setJenkinsSlave(jenkinsJobInput.getJenkinsSlave().trim());

        LOGGER.info("Validation completed giturl {}, branch {}, featureTagFiles line numbers {}"
                , jenkinsJobInput.getGitRepoUrl(), jenkinsJobInput.getBranch(), jenkinsJobInput.getTagsLineNumbers());
        jenkinsJobInput.setBuildTriggerTime(new Date());
        Response response = null;

        LOGGER.info("Fetching Job details {} ", jenkinsJobInput.getPresetName());

        JobInfo jobInfo = fetchJenkinsJobInfo(jenkinsJobInput);


        if (Optional.ofNullable(jobInfo).isPresent()) {
            LOGGER.info("Job already exist, overriding the job: {}  with new input", jenkinsJobInput.getPresetName());

            if (CollectionUtils.isEmpty(jenkinsJobInput.getTagsLineNumbers())) {
                LOGGER.info("No feature tags selected from UI");
                String noTagsSelected = "";
                response = updateAndTriggerExistingJenkinsJob(jenkinsJobInput, noTagsSelected);
            } else {
                for (String tagFile : jenkinsJobInput.getTagsLineNumbers()) {
                    response = updateAndTriggerExistingJenkinsJob(jenkinsJobInput, tagFile);
                }
            }
        } else {
            LOGGER.info("No job found with this name, creating job {}", jenkinsJobInput.getPresetName());

            if (CollectionUtils.isEmpty(jenkinsJobInput.getTagsLineNumbers())) {
                LOGGER.info("no feature tags selected from UI");
                String noTagsSelected = "";
                response = createAndBuildNewJenkinsJob(jenkinsJobInput, noTagsSelected);
            } else {
                for (String tagFile : jenkinsJobInput.getTagsLineNumbers()) {
                    jobInfo = fetchJenkinsJobInfo(jenkinsJobInput);
                    if (Optional.ofNullable(jobInfo).isPresent()) {
                        response = updateAndTriggerExistingJenkinsJob(jenkinsJobInput, tagFile);
                    } else {
                        response = createAndBuildNewJenkinsJob(jenkinsJobInput, tagFile);
                    }
                }
            }

        }
        LOGGER.info("Job {} triggered", jenkinsJobInput.getPresetName());
        return response;
    }

    private Response createAndBuildNewJenkinsJob(JenkinsJobInput jenkinsJobInput, String commaSeparatedTagFiles)
            throws InterruptedException {
        createJenkinsJob(commaSeparatedTagFiles, jenkinsJobInput);

        LOGGER.info("Job created successfully , triggering the job {}", jenkinsJobInput.getPresetName());
        // get Job details and verify if build not triggered
        Response response = buildJenkinsJob(jenkinsJobInput);

        Thread.sleep(20000L);
        return response;
    }

    private Response updateAndTriggerExistingJenkinsJob(JenkinsJobInput jenkinsJobInput, String tagFile)
            throws InterruptedException {
        LOGGER.info("Job {} already exist. overriding the xml with tagfile {} ", jenkinsJobInput.getPresetName(), tagFile);
        overrideConfigXml(tagFile, jenkinsJobInput);

        int lastBuildNumber = 0;

        BuildInfo lastBuildInfo = fetchJenkinsJobInfo(jenkinsJobInput).lastBuild();
        if (Optional.ofNullable(lastBuildInfo).isPresent()) {
            lastBuildNumber = lastBuildInfo.number();
            LOGGER.info("Last Build Number: {} ", lastBuildNumber);
        }

        Response response = buildJenkinsJob(jenkinsJobInput);
        int latestBuildNumber = 0;
        do {
            Thread.sleep(20000L);

            LOGGER.info("Getting latest build number");
            latestBuildNumber = fetchJenkinsJobInfo(jenkinsJobInput).lastBuild().number();
            LOGGER.info("Latest build number {} ", latestBuildNumber);
            if (latestBuildNumber == lastBuildNumber) {
                LOGGER.info(
                        "Last build and latest build numbers are same.new build not started at Jenkins side. waiting for 10 seconds");
            }
        } while (lastBuildNumber >= latestBuildNumber);
        return response;
    }

    private JobInfo fetchJenkinsJobInfo(JenkinsJobInput jenkinsJobInput) {
        LOGGER.info("Fetching jenkins job info");
        return getJenkinsClient(jenkinsJobInput.getJenkinsUrl(),
                jenkinsJobInput.getJenkinsUserName(), jenkinsJobInput.getJenkinsPassword())
                .api()
                .jobsApi()
                .jobInfo(null, jenkinsJobInput.getPresetName());
    }

    private void validateRepositoryUrl(String repositoryUrl) {
        if ((!StringUtils.startsWith(repositoryUrl, "http") || !StringUtils.startsWith(repositoryUrl, "https"))
                && !repositoryUrl.contains(".com/")) {
            throw new IncorrectInputException("Repository URL format is not correct");
        }
    }

    private Response createJenkinsJob(String commaSeparatedTagFiles, JenkinsJobInput jenkinsJobInput) {

        String mvnTestCommand = prepareMavenTestCommand(commaSeparatedTagFiles, jenkinsJobInput.getTags(), jenkinsJobInput.getParallelBuilds(), jenkinsJobInput.getExecutionPresetConfig());

        //Uncomment above method call for executing on local Jenkins (Non Docker)
        String jenkinsConfigXml = jenkinsJobXmlService.prepareJenkinsConfigurationXml(mvnTestCommand, jenkinsJobInput);
        LOGGER.info("Configuration xml ready. creating job in jenkins");

        RequestStatus status = getJenkinsClient(jenkinsJobInput.getJenkinsUrl(), jenkinsJobInput.getJenkinsUserName(), jenkinsJobInput.getJenkinsPassword())
                .api().jobsApi().create(null, jenkinsJobInput.getPresetName(), jenkinsConfigXml);

        Response response = Response.builder().build();

        if (status.value().booleanValue()) {
            response.setMessage("Job has created successfully");
        } else {
            if (status.errors() != null) {
                response.setErrors(buildError(status.errors()));
            }
            response.setMessage("Job already exists with the name");
        }
        return response;
    }

    private Response buildJenkinsJob(JenkinsJobInput jenkinsJobInput) {
        LOGGER.info("Triggering the job {}", jenkinsJobInput.getPresetName());
        IntegerResponse response = getJenkinsClient(jenkinsJobInput.getJenkinsUrl(), jenkinsJobInput.getJenkinsUserName(), jenkinsJobInput.getJenkinsPassword())
                .api().jobsApi().build(null, jenkinsJobInput.getPresetName());
        LOGGER.info("Job {} triggered successfully", jenkinsJobInput.getPresetName());


        Response serviceResponse = Response.builder().build();

        if (response.value() != null) {
            serviceResponse.setMessage("Job has invoked successfully");
        } else {
            if (response.errors() != null) {
                serviceResponse.setErrors(buildError(response.errors()));
            }
        }
        return serviceResponse;

    }

    private void overrideConfigXml(String tags,
                                   JenkinsJobInput jenkinsJobInput) {
        String mvnTestCommand = prepareMavenTestCommand(tags, jenkinsJobInput.getTags(), jenkinsJobInput.getParallelBuilds(), jenkinsJobInput.getExecutionPresetConfig());

        //Uncomment above method call for executing on local Jenkins (Non Docker)
        String jenkinsConfigXml = jenkinsJobXmlService.prepareJenkinsConfigurationXml(mvnTestCommand, jenkinsJobInput);
        LOGGER.info("Configuration xml ready. overriding in jenkins");

        getJenkinsClient(jenkinsJobInput.getJenkinsUrl(), jenkinsJobInput.getJenkinsUserName(), jenkinsJobInput.getJenkinsPassword())
                .api().jobsApi().config(null, jenkinsJobInput.getPresetName(), jenkinsConfigXml);
        LOGGER.info("Jenkins job {} is ready with latest configuration xml", jenkinsJobInput.getPresetName());

    }

    private String prepareMavenTestCommand(String commaSeparatedTagFiles, String tags, String parallelBuild, String executionPresetConfig) {
        String mavenCommand = null;
//        if (StringUtils.isBlank(commaSeparatedTagFiles)) {
//            mavenCommand = new StringBuilder("clean test -Dcucumber.options=")
//                    .append("&quot;").append("--tags ~@nonexistantTag").append("&quot;")
//                    .append(" ").append(executionPresetConfig).toString();
//        } else {
//            mvn test -Dcucumber.options="--tags '@debug1 or @debug2'"

//            mvn clean test '--Dcucumber.filter.tags={@patch' or @get or '@api}'
//            StringBuilder command = new StringBuilder("clean test -Dcucumber.options=\"--tags ");
            StringBuilder command = new StringBuilder("clean test -Dcucumber.filter.tags=\"");
            command.append(tags);
            command.append("\" -Dcucumber.execution.parallel.config.strategy=fixed -Dcucumber.execution.parallel.enabled=true ");
            command.append("-Dcucumber.execution.parallel.config.fixed.parallelism=").append(parallelBuild).append(" ");
            mavenCommand = command.append(executionPresetConfig).toString().replace(",}", "}");




//            StringBuilder command = new StringBuilder("clean test -Dfeatures={");
//            for (String tagFile : commaSeparatedTagFiles.split(",")) {
//                command.append("\"").append(tagFile).append("\"").append(",");
//            }
//            mavenCommand = command.append("} ").append(executionPresetConfig).toString().replace(",}", "}");
//        }
        LOGGER.info("Maven command {}", mavenCommand);
        return mavenCommand;
    }

    private void validateInputData(String... inputValues) {
        Arrays.asList(inputValues).forEach(e -> {
            if (StringUtils.isBlank(e)) {
                throw new IncorrectInputException("input is not correct. some fields are empty");
            }
        });
    }

    public JenkinsBuildInfo fetchLatestBuildInfo(String jobName) {
        JenkinsPreset jenkinsPreset = testingPresetService.getJenkinsPresetByTestPresetName(jobName);
        Integer lastBuildNumber = getJenkinsClient(jenkinsPreset.getUrl(),
                jenkinsPreset.getUserName(), jenkinsPreset.getPassword()).api().jobsApi().
                lastBuildNumber(null, jobName);
//        Integer lastBuildNumber = jenkinsClient.api().jobsApi().lastBuildNumber(null, jobName);

        if (lastBuildNumber == null) {
            throw new NoRecordFoundException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
        }

        BuildInfo buildInfo = getJenkinsClient(jenkinsPreset.getUrl(),
                jenkinsPreset.getUserName(), jenkinsPreset.getPassword()).api().jobsApi().
                buildInfo(null, jobName, lastBuildNumber);
//        BuildInfo buildInfo = jenkinsClient.api().jobsApi().buildInfo(null, jobName, lastBuildNumber);

        return JenkinsBuildInfo.builder().actions(buildAction(buildInfo.actions()))
                .building(buildInfo.building()).duration(buildInfo.duration())
                .result(buildInfo.result())
                .fullDisplayName(buildInfo.fullDisplayName()).estimatedDuration(buildInfo.estimatedDuration()).build();
    }

    public Map<String, Object> fetchAllBuildInfo(int page, int size) {
        LOGGER.info("Fetching Test preset name ");
        List<String> testPresetNames = testingPresetService.getAllTestPresetsName();
        List<JenkinsBuildInfo> jenkinsBuildInfoList = new ArrayList<>();
        for (String jobName : testPresetNames) {
            JenkinsPreset jenkinsPreset = testingPresetService.getJenkinsPresetByTestPresetName(jobName);
//            Integer lastBuildNumber = jenkinsClient.api().jobsApi().lastBuildNumber(null, jobName);
            Integer lastBuildNumber = getJenkinsClient(jenkinsPreset.getUrl(),
                    jenkinsPreset.getUserName(), jenkinsPreset.getPassword()).api().jobsApi().
                    lastBuildNumber(null, jobName);
            if (lastBuildNumber != null) {
                BuildInfo buildInfo = getJenkinsClient(jenkinsPreset.getUrl(),
                        jenkinsPreset.getUserName(), jenkinsPreset.getPassword()).api().jobsApi().
                        buildInfo(null, jobName, lastBuildNumber);
//                BuildInfo buildInfo = jenkinsClient.api().jobsApi().buildInfo(null, jobName, lastBuildNumber);
                jenkinsBuildInfoList.add(JenkinsBuildInfo.builder().actions(buildAction(buildInfo.actions()))
                        .building(buildInfo.building()).duration(buildInfo.duration())
                        .result(buildInfo.result())
                        .fullDisplayName(buildInfo.fullDisplayName()).estimatedDuration(buildInfo.estimatedDuration())
                        .build());
            }
        }
        return GenericUtils.getPaginatedResponse(GenericUtils.getPages(jenkinsBuildInfoList, page, size), page, size, jenkinsBuildInfoList.size());
    }

    public File downloadLogs(String jobName, int buildNumber) {
        LOGGER.info("Fetching Logs for Build # {} for Job {}", buildNumber, jobName);
        JenkinsPreset jenkinsPreset = testingPresetService.getJenkinsPresetByTestPresetName(jobName);

//        ProgressiveText progressiveText = jenkinsClient.api().jobsApi().progressiveText(null, jobName, buildNumber, 0);
        ProgressiveText progressiveText = getJenkinsClient(jenkinsPreset.getUrl(),
                jenkinsPreset.getUserName(), jenkinsPreset.getPassword())
                .api().jobsApi().progressiveText(null, jobName, buildNumber, 0);

        File file = new File(jobName + "-" + buildNumber + ".txt");

        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(progressiveText.text());
            LOGGER.info("Jenkins Console Logs has been added to logs file");
            return file;
        } catch (IOException e) {
            LOGGER.info("File not found");
            throw new NoResourceFoundException("File not found");
        }
    }

    public BuildInfo fetchBuildInfo(String jobName, int buildNumber) {
//        return jenkinsClient.api().jobsApi().buildInfo(null, jobName, buildNumber);
        JenkinsPreset jenkinsPreset = testingPresetService.getJenkinsPresetByTestPresetName(jobName);
        return getJenkinsClient(jenkinsPreset.getUrl(),
                jenkinsPreset.getUserName(), jenkinsPreset.getPassword()).api().jobsApi().
                buildInfo(null, jobName, buildNumber);
    }

    private List<Action> buildAction(List<com.cdancy.jenkins.rest.domain.job.Action> actions) {
        return Optional.ofNullable(actions).map(Collection::stream).orElseGet(Stream::empty).map(action -> {
            List<Cause> causes = buildCause(action.causes());
            List<Parameter> parameters = buildParameters(action.parameters());
            if (CollectionUtils.isEmpty(causes) && CollectionUtils.isEmpty(parameters)) {
                return null;
            } else {
                return Action.builder().causes(causes).parameters(parameters).build();
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Cause> buildCause(List<com.cdancy.jenkins.rest.domain.job.Cause> causes) {
        return Optional.ofNullable(causes).filter(CollectionUtils::isNotEmpty)
                .map(Collection::stream).orElseGet(Stream::empty).filter(Objects::nonNull).map(cause ->
                        Cause.builder().clazz(cause.clazz()).shortDescription(cause.shortDescription())
                                .userId(cause.userId()).userName(cause.userName()).build()
                ).collect(Collectors.toList());
    }

    private List<Parameter> buildParameters(List<com.cdancy.jenkins.rest.domain.job.Parameter> parameters) {
        return Optional.ofNullable(parameters).filter(CollectionUtils::isNotEmpty)
                .map(Collection::stream).orElseGet(Stream::empty).filter(Objects::nonNull).map(parameter ->
                        Parameter.builder().clazz(parameter.clazz()).name(parameter.name()).value(parameter.value())
                                .build()
                ).collect(Collectors.toList());
    }

    private String jenkinsConfigurationXml(String jobName) {
        return new StringBuilder(JENKINS_JOB_CONFIG_XML1).append("<description>").append(jobName)
                .append("</description>")
                .append("<keepDependencies>false</keepDependencies><properties/><definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2.87\">")
                .append("<script>").append("pipeline").append("{").append("\n").append("\t").append("agent any")
                .append("\n").append("\n").append("\t\t").append("stages").append("{").append("\n").append("\t\t\t")
                .append("stage").append("(").append("&apos;").append("Hello").append("&apos;").append(")").append("{")
                .append("\n").append("\t\t\t\t").append("steps").append("{").append("\n").append("\t\t\t\t\t")
                .append("echo ").append("&apos;").append("Hello World").append("&apos;").append("\n").append("\t\t\t\t")
                .append("}").append("\n").append("\t\t\t").append("}").append("\n").append("\t\t").append("}")
                .append("\n").append("\t").append("}").append("\n")
                .append("</script><sandbox>true</sandbox></definition><triggers/><disabled>false</disabled></flow-definition>")
                .toString();
    }
}
