package com.nisum.gitlab.domain;

import com.nisum.domain.GitBaseRequest;
import lombok.Data;

@Data
public class GitLabInfoRequest extends GitBaseRequest {
    private String repositoryPathName;
    private String searchElement;
    private String filesLanguage;

    @Override
    public String toString() {
        return "GitlabBranchesUserInput [gitlabUserName=" + userName + ", repositoryName=" + getRepositoryName() + ", repositoryPathName=" + repositoryPathName + ", repoUrl="
                + repoUrl + ", branch=" + getBranch() + ", searchElement=" + searchElement + ", filesLanguage="
                + filesLanguage + ", commaSeparatedTags=" + getCommaSeparatedTags() + "]";
    }
}
