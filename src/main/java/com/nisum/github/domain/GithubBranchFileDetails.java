package com.nisum.github.domain;

import lombok.Data;

@Data
public class GithubBranchFileDetails {
	private String filename;
	// Possible statuses
	// added modified removed
	private String status;
	private String raw_url;

}
