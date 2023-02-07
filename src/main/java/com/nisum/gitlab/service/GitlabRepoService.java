package com.nisum.gitlab.service;

import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.exception.custom.NoResourceFoundException;
import com.nisum.gitlab.domain.GitLabInfoRequest;
import com.nisum.gitlab.domain.GitlabBranchReponse;
import com.nisum.gitlab.domain.GitlabNameResponse;
import com.nisum.util.GitRepoConstants;
import com.nisum.util.InputValidatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile({"dev", "qa"})
public class GitlabRepoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabRepoService.class);

    @Autowired
    private WebClient webClient;

    @Value("${gitlab.baseapiurl}")
    private String gitlabBaseApiUrl;

    @Value("${gitlab.nisum.baseapiurl}")
    private String mynisumGitlabBaseApiUrl;

    public List<String> fetchGitlabBranches(GitLabInfoRequest request) {
        request.setAccessToken(
                InputValidatorUtil.addDefaultValueIfAccessTokenIsEmpty(request.getAccessToken()));
        validateInputValues(request.getUserName(), request.getRepoUrl(),
                request.getAccessToken());
        request.setUserName(InputValidatorUtil.trimInputString(request.getUserName()));
        request.setRepoUrl(InputValidatorUtil.trimInputString(request.getRepoUrl()));
        request.setAccessToken(InputValidatorUtil.trimInputString(request.getAccessToken()));

        validateRepositoryUrl(request.getRepoUrl());

        request.setBaseApiUrl(getBaseApiUrl(request.getRepoUrl()));

        request.setRepositoryPathName(InputValidatorUtil.extractRepositoryPathNameFromRepoUrl(request.getRepoUrl()));

        request.setRepositoryName(InputValidatorUtil.extractRepositoryNameFromRepoUrl(request.getRepoUrl()));

        String projectId = fetchProjectId(request);

        return fetchGitlabBranches(request, projectId);
    }


    private List<String> fetchGitlabBranches(GitLabInfoRequest request, String projectId) {
        String branchesUrl = request.getBaseApiUrl() + "/projects/" + projectId + "/repository/branches";
        LOGGER.info("branches api url to retrieve branch names {}", branchesUrl);
        Flux<GitlabBranchReponse> f = webClient.get()
                .uri(URI.create(branchesUrl))
                .header("Private-Token", request.getAccessToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(GitlabBranchReponse.class);
        List<String> branchNames = f.toStream().map(GitlabBranchReponse::getName)
                .collect(Collectors.toList());
        LOGGER.info("{} branches found for projectId {} : {} ", branchNames.size(), projectId, branchNames);
        return branchNames;
    }

    protected String fetchProjectId(GitLabInfoRequest request) {
        LOGGER.info("Fetching projectId...");
        String projectApiUrl = request.getBaseApiUrl() + "/projects?search=" + request.getRepositoryName();
        LOGGER.info("projectApiUrl to retrieve projectId {}", projectApiUrl);
        Flux<GitlabNameResponse> gitlabnameResponseFlux = webClient.get()
                .uri(URI.create(projectApiUrl))
                .header("Private-Token", request.getAccessToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(GitlabNameResponse.class);
        GitlabNameResponse gitlabNameResponse = gitlabnameResponseFlux.filter(element ->
                        StringUtils.equalsIgnoreCase(request.getRepositoryPathName(), element.getPath_with_namespace()))
                .blockFirst();
        if (!Optional.ofNullable(gitlabNameResponse).isPresent()) {
            LOGGER.info("No Projects Found");
            throw new NoResourceFoundException("No Projects Found");
        }

        LOGGER.info("Project Id : {}", gitlabNameResponse.getId());
        return gitlabNameResponse.getId();
    }

    protected String getBaseApiUrl(String repositoryUrl) {
        if (StringUtils.containsIgnoreCase(repositoryUrl, GitRepoConstants.GITLAB_URL_FORMAT)) {
            return gitlabBaseApiUrl;
        } else if (StringUtils.containsIgnoreCase(repositoryUrl, GitRepoConstants.NISUM_GITLAB_URL_FORMAT)) {
            return mynisumGitlabBaseApiUrl;
        } else {
            throw new IncorrectInputException("this repository is not supported in this project");
        }
    }

    protected void validateRepositoryUrl(String repositoryUrl) {
        if ((!StringUtils.startsWith(repositoryUrl, "http") || !StringUtils.startsWith(repositoryUrl, "https"))
                && !repositoryUrl.contains(".com/")) {
            throw new IncorrectInputException("repository URL format is not correct");
        }
    }

    protected void validateInputValues(String... inputValues) {
        Arrays.asList(inputValues).forEach(e -> {
            if (InputValidatorUtil.isInputEmpty(e)) {
                throw new IncorrectInputException(GitRepoConstants.INPUT_FIELDS_EMPTY_MSG);
            }
        });
    }

}
