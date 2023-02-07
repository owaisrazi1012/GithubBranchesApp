package com.nisum.jenkins.service.impl;

import com.nisum.jenkins.domain.JenkinsJobInput;
import com.nisum.jenkins.service.JenkinsJobXmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Profile({"dev", "qa"})
public class JenkinsJobXmlServiceImpl implements JenkinsJobXmlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsJobXmlServiceImpl.class);

    @Value("${server.baseurl}")
    private String serverBaseURL;

    @Value("${server.port}")
    private String serverPort;


    public String prepareJenkinsConfigurationXml(String mvnTestCommand, JenkinsJobInput jenkinsJobInput) {
        LOGGER.info("preparing configuration xml ");
        return new StringBuilder("<?xml version='1.1' encoding='UTF-8'?>\n")
                .append("<flow-definition plugin=\"workflow-job@2.40\"> \n")
                .append("\t").append("<actions>\n")
                .append("\t\t").append("<org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction plugin=\"pipeline-model-definition@1.7.2\"/>\n")
                .append("\t\t").append("<org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction plugin=\"pipeline-model-definition@1.7.2\">\n")
                .append("\t\t\t").append("<jobProperties/>\n")
                .append("\t\t\t").append("<triggers/>\n")
                .append("\t\t\t").append("<parameters/>\n")
                .append("\t\t\t").append("<options/>\n")
                .append("\t\t").append("</org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction>\n")
                .append("\t").append("</actions>\n")
                .append("<description>").append("jenkins job on cucumber tests : ").append(jenkinsJobInput.getPresetName()).append("</description>\n")
                .append("<keepDependencies>false</keepDependencies>\n")
                .append("<properties/>\n")
                .append("<definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2.87\">\n")
                .append("<script>")
                .append("pipeline ").append("{\n")
                .append("\t").append(agentBlock(jenkinsJobInput.getJenkinsSlave()))
                .append("\t").append("tools ").append("{\n")
                .append("\t\t").append("maven &apos;maven&apos; \n")
                .append("\t").append("} \n")
                .append("\t").append("stages ").append("{\n")
                .append("\t\t").append(initialization())
                .append("\t\t").append(gitCloning(jenkinsJobInput.getGitRepoUrl(), jenkinsJobInput))
                .append("\t\t").append(buildJob(mvnTestCommand))
                .append("\t").append("}\n")
                .append("\t").append("post ").append("{\n")
                .append("\t\t").append(alwaysBlock(jenkinsJobInput.getBuildTriggerTime()))
                //.append("\t\t").append(emailSendingBlock("failure", "Failed", jenkinsJobInput.getNotificationPresetConfig()))
                //.append("\t\t").append(emailSendingBlock("success", "Successful", jenkinsJobInput.getNotificationPresetConfig()))
                .append("\t").append("}\n")
                .append("}\n")
                .append("</script>\n")
                .append("<sandbox>true</sandbox>\n")
                .append("</definition>\n")
                .append("<triggers/>\n")
                .append("<disabled>false</disabled>\n")
                .append("</flow-definition>\n")
                .toString();
    }

    private Object agentBlock(String agentNames) {
        return new StringBuilder("agent ")
                .append("{\n")
                .append("\t\t").append("label ").append("{\n")
                .append("\t\t\t").append("label ")
                .append("&apos;").append(agentNames).append("&apos;").append("\n")
                .append("\t\t").append("}\n")
                .append("\t").append("}\n");
    }

    private String initialization() {
        LOGGER.info("Initialization block ");
        return new StringBuilder("stage ").append("(")
                .append("&apos;").append("Initialization").append("&apos;").append(")").append(" {\n")
                .append("\t\t").append("steps ").append("{\n")
                .append("\t\t\t").append("sh &apos;&apos;&apos; \n")
                .append("\t\t\t\t").append("echo &quot;PATH = ${PATH}&quot;\n")
                .append("\t\t\t\t").append("echo &quot;M2_HOME = ${M2_HOME}&quot;\n")
                .append("\t\t\t").append("&apos;&apos;&apos;\n")
                .append("\t\t").append("}\n")
                .append("\t").append("}\n")
                .toString();
    }

    private String gitCloning(String gitRepoUrl, JenkinsJobInput jenkinsJobInput) {
        LOGGER.info("Git cloning block ");
        return new StringBuilder("stage ").append("(")
                .append("&apos;").append("Git cloning").append("&apos;").append(")").append(" {\n")
                .append("\t\t").append("steps ").append("{\n")
                .append("\t\t\t").append("git branch: ").append("&apos;").append(jenkinsJobInput.getBranch()).append("&apos;")
                .append(", ").append("credentialsId: ").append("&apos;").append(jenkinsJobInput.getCredentialId()).append("&apos;")
                .append(", ").append("url: ").append("&apos;").append(gitRepoUrl).append("&apos;\n")
                .append("\t\t").append("}\n")
                .append("\t").append("}\n")
                .toString();
    }

    private String buildJob(String mvnTestCommand) {
        LOGGER.info("Build job block ");
        return new StringBuilder("stage ").append("(")
                .append("&apos;").append("Build").append("&apos;").append(")").append(" {\n")
                .append("\t\t").append("steps ").append("{\n")
                .append("\t\t\t").append("sh &apos;").append("mvn ").append(mvnTestCommand).append("&apos;\n")
                .append("\t\t\t").append("cucumber buildStatus: ").append("&quot;").append("SUCCESS").append("&quot;")
                .append(",\n")
                .append("\t\t\t").append("fileIncludePattern: ").append("&quot;").append("cucumber.json").append("&quot;")
                .append(",\n")
                .append("\t\t\t").append("jsonReportDirectory: ").append("&apos;").append("target").append("&apos;\n")
                .append("\t\t").append("}\n")
                .append("\t").append("}\n")
                .toString();
    }

    private String alwaysBlock(Date buildTriggerTime) {
        LOGGER.info(" post - always block ");
        return new StringBuilder("always").append(" {\n")
                .append("\t\t\t")
                .append("sh ''' cd target ")
                .append("\n\t\t\t")
                .append(" curl -k --location --request POST 'https://").append(serverBaseURL).append(":").append(serverPort).append("/GithubBranchesApp/post-jenkins-actions/' \\")
                .append("\n\t\t\t")
                .append("--header 'cache-control: no-cache' \\")
                .append("\n\t\t\t")
                .append("--form 'project=\"'$JOB_NAME'\"' \\")
                .append("\n\t\t\t")
                .append("--form 'buildDate=\"").append(buildTriggerTime.getTime()).append("\"' \\")
                .append("\n\t\t\t")
                .append("--form 'buildNumber=\"'$BUILD_NUMBER'\"' \\")
                .append("\n\t\t\t")
                .append("--form 'file=@\"cucumber.json\"'")
                .append("\n\t\t\t")
                .append("'''")
                .append("\t\t").append("\n}\n")
                .toString();
    }


    private String emailSendingBlock(String scriptName, String buildStatus, String emailReceipents) {
        LOGGER.info("Email sending block ");
        return new StringBuilder(scriptName).append(" {\n")
                .append("\t\t\t").append("script ").append("{\n")
                .append("\t\t\t\t").append("emailext ")
                .append("attachmentsPattern: ")
                .append("&apos;").append("emailable-report.html").append("&apos;").append(", ")
                .append("body: ").append("&apos;&apos;&apos;")
                .append("${SCRIPT, template=").append("&quot;").append("groovy-html.template")
                .append("&quot;").append("}")
                .append("&apos;&apos;").append("${FILE, path=").append("&quot;").append("emailable-report.html").append("&quot;")
                .append("}").append("&apos;&apos;&apos;").append(",\n")
                .append("\t\t\t\t").append("subject: ")
                .append("&quot;").append("${env.JOB_NAME}")
                .append(" - Build # ${env.BUILD_NUMBER} - ")
                .append(buildStatus).append("&quot;").append(",\n")
                .append("\t\t\t\t").append("mimeType: ")
                .append("&apos;text/html&apos;").append(",")
                .append("to: ").append("&quot;").append(emailReceipents).append("&quot;")
                .append("\t\t").append("}\n")
                .append("\t").append("}\n")
                .toString();
    }
}
