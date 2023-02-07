package com.nisum.domain;

import lombok.Data;

@Data
public class GitBaseRequest {
    protected String userName;
    protected String repoUrl;
    protected String accessToken;
    private String repositoryName;
    private String branch;
    private String commaSeparatedTags;
    private Integer parallelBuilds;
    private String baseApiUrl;
}
