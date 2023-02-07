package com.nisum.github.domain;

import lombok.Data;

import java.util.List;

@Data
public class GithubAllBranchCommit {
	private String sha;
	private String url;
	List<GithubBranchFileDetails> files;
	List<ParentGithubCommit> parents;
	
}
