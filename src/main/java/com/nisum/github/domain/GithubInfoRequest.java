package com.nisum.github.domain;

import com.nisum.domain.GitBaseRequest;
import lombok.Data;

@Data
public class GithubInfoRequest extends GitBaseRequest {

	private String searchElement;
	private String filesLanguage;
	
	@Override
	public String toString() {
		return "GitBranchesUserInput [githubUserName=" + userName + ", repositoryName=" + getRepositoryName() + ", repositoryUrl="
				+ repoUrl + ", branch=" + getBranch() + ", searchElement=" + searchElement + ", filesLanguage="
				+ filesLanguage + ", commaSeparatedTags=" + getCommaSeparatedTags() +"]";
	}
	
}