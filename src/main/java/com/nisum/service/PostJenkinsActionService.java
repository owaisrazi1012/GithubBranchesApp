package com.nisum.service;

import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.nisum.api.preset.domain.dto.EmailDetails;
import com.nisum.api.preset.domain.entity.JenkinsBuildInfo;

import java.util.Date;

public interface PostJenkinsActionService {

    // To send an email with attachment
    boolean sendEmail(EmailDetails emailDetails) throws Exception;

    boolean saveJenkinsBuildInfo(BuildInfo buildInfo, String project, long buildDate, String attachment
            , Boolean isFailureScenarioExist);
}
