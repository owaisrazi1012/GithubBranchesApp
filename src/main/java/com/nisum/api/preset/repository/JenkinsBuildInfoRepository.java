package com.nisum.api.preset.repository;

import com.nisum.api.preset.domain.entity.JenkinsBuildInfo;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface JenkinsBuildInfoRepository extends PagingAndSortingRepository<JenkinsBuildInfo, Long> {

    List<JenkinsBuildInfo> findByIsEmailSentOrIsJiraCreated(boolean isEmailSent, boolean IsJiraCreated);

    List<JenkinsBuildInfo> findByIsEmailSentAndIsJiraCreated(boolean isEmailSent, boolean IsJiraCreated);

    List<JenkinsBuildInfo> findByBuildDateAndIsEmailSent(Long buildDate, boolean isEmailSent);

    List<JenkinsBuildInfo> findByBuildDateAndAndIsJiraCreated(Long buildDate, boolean isJiraCreated);
}
