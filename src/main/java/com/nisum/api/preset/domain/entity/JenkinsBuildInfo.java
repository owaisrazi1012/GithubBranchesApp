package com.nisum.api.preset.domain.entity;

import com.nisum.elasticsearch.domain.BuildStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "jenkins_build_info")
public class JenkinsBuildInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String attachment;
	@Lob
	private String failedTests;
	private String jobName;
	private String buildNumber;
	private long buildDate;
	private BuildStatus buildStatus;
	private boolean isEmailSent;
	private boolean isJiraCreated;
	private Date date;
}
