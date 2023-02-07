package com.nisum.gitlab.domain;

import lombok.Data;

@Data
public class GitlabSearchResult {
	
	private String filename;
	private String ref;
	private Integer project_id;

}
