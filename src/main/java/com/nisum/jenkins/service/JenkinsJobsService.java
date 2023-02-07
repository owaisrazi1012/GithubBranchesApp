package com.nisum.jenkins.service;

import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.nisum.jenkins.domain.JenkinsBuildInfo;
import com.nisum.jenkins.domain.JenkinsJob;
import com.nisum.jenkins.domain.JenkinsJobInput;
import com.nisum.jenkins.domain.Response;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface JenkinsJobsService {

	List<JenkinsJob> fetchAllJenkinsJobs();

	 Response createJenkinsJob(String jobName);

	 Response triggerJenkinsJob(JenkinsJobInput jenkinsJobInput) throws Exception;

	 JenkinsBuildInfo fetchLatestBuildInfo(String jobName);

	Map<String, Object> fetchAllBuildInfo(int page, int size);

	File downloadLogs(String jobName, int buildNumber);

	BuildInfo fetchBuildInfo(String jobName, int buildNumber);

}
