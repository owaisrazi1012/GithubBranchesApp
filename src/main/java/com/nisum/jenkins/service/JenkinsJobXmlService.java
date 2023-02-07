package com.nisum.jenkins.service;

import com.nisum.jenkins.domain.JenkinsJobInput;

public interface JenkinsJobXmlService {
    String prepareJenkinsConfigurationXml(String mvnTestCommand, JenkinsJobInput jenkinsJobInput);
}
