package com.nisum.github.domain;

import lombok.Data;

import java.util.List;

@Data
public class GithubMasterTagsResponse {
	
	List<GithubMasterFilesDetails> items;
	

}
