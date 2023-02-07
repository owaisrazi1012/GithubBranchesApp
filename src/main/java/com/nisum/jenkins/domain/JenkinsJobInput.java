package com.nisum.jenkins.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class JenkinsJobInput {
	private String gitRepoUrl;
	private String gitUserName;
	private String credentialId;
	private String jenkinsSlave;
	private String branch;
	private String tags;
	private String parallelBuilds;
	private List<String> tagsLineNumbers;
	private String cron;
	private String executionPresetConfig;
	private String notificationPresetConfig;
	private String presetName;
	private String jenkinsUrl;
	private String jenkinsUserName;
	private String jenkinsPassword;
	private Date buildTriggerTime;

	@Override
	public String toString() {
		return "JenkinsJobInput [gitRepoUrl=" + gitRepoUrl + ", gitUserName=" + gitUserName + ", jenkinsSlave=" + jenkinsSlave
				+ ", branch=" + branch+ ", tagsLineNumbers=" + tagsLineNumbers + ", cron=" + cron + ", executionPresetConfig="
				+ executionPresetConfig + ", notificationPresetConfig=" + notificationPresetConfig + ", presetName="
				+ presetName + ", jenkinsUrl=" + jenkinsUrl + ", jenkinsUserName=" + jenkinsUserName + "]";
	}
}
