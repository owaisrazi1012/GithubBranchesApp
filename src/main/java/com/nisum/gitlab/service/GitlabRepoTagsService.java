package com.nisum.gitlab.service;

import com.nisum.domain.TagLinesDetails;
import com.nisum.gitlab.domain.AllCommitDetails;
import com.nisum.gitlab.domain.CommitDetailsDiff;
import com.nisum.gitlab.domain.GitLabInfoRequest;
import com.nisum.gitlab.domain.GitlabSearchResult;
import com.nisum.service.VersionControlService;
import com.nisum.util.GitRepoConstants;
import com.nisum.util.InputValidatorUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Profile({"dev", "qa"})
public class GitlabRepoTagsService {

    @Autowired
    private GitlabRepoService gitlabRepoService;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private WebClient webClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabRepoTagsService.class);

    public Set<String> retrieveAllTags(GitLabInfoRequest request) throws Exception {
        request = this.validateGitLabRequest(request);

        String projectId = gitlabRepoService.fetchProjectId(request);

        Set<String> featureFilePaths = this.getProjectFilesPath(request, projectId);

        Set<String> tags = new HashSet<>();
        for (String featureFilePath : featureFilePaths) {
            try {
                InputStream inputStream = this.getInputStream(featureFilePath, request.getBaseApiUrl(), projectId, request.getBranch(), request.getAccessToken());
                IOUtils.readLines(inputStream, StandardCharsets.UTF_8)
                        .stream()
                        .forEach(line -> {
                            line = line.trim();
                            if (StringUtils.startsWith(line, "@")) {
                                List<String> lineTags = Arrays.asList(line.split("@"));
                                lineTags.stream()
                                        .filter(StringUtils::isNotBlank)
                                        .forEach(tag -> {
                                            tags.add(tag.trim());
                                        });
                            }
                        });

            } catch (FileNotFoundException e) {
                LOGGER.info("This file {} might be removed from branch", featureFilePath);
            }
        }
        LOGGER.info("Tags with url are: {}", tags);
        return tags;
    }

    public List<TagLinesDetails> retrieveFeatureLinesForBuild(GitLabInfoRequest request) throws Exception {

        request = this.validateGitLabRequest(request);

        String projectId = gitlabRepoService.fetchProjectId(request);

        Set<String> featureFilePaths = this.getProjectFilesPath(request, projectId);

        return this.retrieveFeatureFilesTags(request, featureFilePaths, projectId);
    }

    private List<TagLinesDetails> retrieveFeatureFilesTags(GitLabInfoRequest request, Set<String> featureFilePaths, String projectId) throws Exception {
        Map<String, TagLinesDetails> fileTagsWithLinesMap = new HashMap<>();
        List<TagLinesDetails> tagLinesDetailsList = new ArrayList<>();
        List<String> tags = new ArrayList<>(Arrays.asList(request.getCommaSeparatedTags().split(",")));
        for (String featureFilePath : featureFilePaths) {
            try {
                String urlString = request.getBaseApiUrl() + "/projects/" + projectId + "/repository/files/" + featureFilePath.replaceAll("/", "%2F") + "/raw?ref=" + request.getBranch();
                LOGGER.info("reading file content of {} from url {}", featureFilePath, urlString);
                URL url = new URL(urlString);
                URLConnection uc = url.openConnection();
                uc.setRequestProperty("Private-Token", request.getAccessToken());
                InputStream inputStream = uc.getInputStream();

                AtomicInteger ordinal = InputValidatorUtil.getFeatureFileInitialLineNumber();
                IOUtils.readLines(inputStream, StandardCharsets.UTF_8)
                        .stream()
                        .forEach(line -> {
                            line = line.trim();
                            if (StringUtils.startsWith(line, "@")) {
                                List<String> lineTags = Arrays.asList(line.split("@"));
                                lineTags.stream()
                                        .filter(StringUtils::isNotBlank)
                                        .forEach(tag -> {
                                            tag = tag.trim();
                                            if (tags.contains(tag)) {
                                                if (!fileTagsWithLinesMap.containsKey(featureFilePath + ":" + ordinal.get())) {
                                                    tagLinesDetailsList.add(new TagLinesDetails(tag, featureFilePath, ordinal.get()));
                                                    fileTagsWithLinesMap.put(featureFilePath + ":" + ordinal.get(), new TagLinesDetails(tag, featureFilePath, ordinal.get()));
                                                }
                                            }
                                        });
                            }
                            ordinal.getAndIncrement();
                        });

                LOGGER.debug("After reading file content of file {} , tagslinenumbers map {}", featureFilePath, fileTagsWithLinesMap);

            } catch (FileNotFoundException e) {
                LOGGER.info("This file {} might be removed from branch", featureFilePath);
            }
        }
        LOGGER.info("Files tags with url map size {} , data {}", tagLinesDetailsList.size(), tagLinesDetailsList);
        return tagLinesDetailsList;
    }

    private Set<String> getProjectFilesPath(GitLabInfoRequest request, String projectId) {
        String treeApiUrl = request.getBaseApiUrl() + "/projects/" + projectId + "/search?scope=blobs&search=feature+extension:feature";
        LOGGER.info("projectApiUrl to retrieve project files tree {}", treeApiUrl);
        List<GitlabSearchResult> gitlabnameFilesList = getWebClientResponse(treeApiUrl, request.getAccessToken())
                .bodyToFlux(GitlabSearchResult.class)
                .toStream().collect(Collectors.toList());
        Set<String> featureFilePaths = new HashSet<>();
        if (CollectionUtils.isNotEmpty(gitlabnameFilesList)) {
            featureFilePaths.addAll(extractAllFilesPaths(gitlabnameFilesList));
        }
        LOGGER.info("{} fearture files found on master {}", featureFilePaths.size(), featureFilePaths);
        if (!StringUtils.equals(GitRepoConstants.MASTER, request.getBranch())) {
            String masterCommitsUrl = request.getBaseApiUrl() + "/projects/" + projectId + "/repository/commits?ref_name=" + GitRepoConstants.MASTER;
            String branchCommitsUrl = request.getBaseApiUrl() + "/projects/" + projectId + "/repository/commits?ref_name=" + request.getBranch();
            AllCommitDetails masterCommit = getWebClientResponse(masterCommitsUrl, request.getAccessToken())
                    .bodyToFlux(AllCommitDetails.class)
                    .blockFirst();
            List<AllCommitDetails> branchCommits = getWebClientResponse(branchCommitsUrl, request.getAccessToken())
                    .bodyToFlux(AllCommitDetails.class)
                    .toStream().collect(Collectors.toList());
            for (AllCommitDetails commit : branchCommits) {
                LOGGER.info("comparing commits branch {} master {} ", commit.getId(), masterCommit.getId());
                if (CollectionUtils.isEmpty(commit.getParentsIds()) || commit.getId().contains(masterCommit.getId())) {
                    LOGGER.info("both shas matched ");
                    break;
                }
                String commitDetailsUrl = request.getBaseApiUrl() + "/projects/" + projectId + "/repository/commits/"
                        + commit.getId() + "/diff";
                List<CommitDetailsDiff> commitDetails = getWebClientResponse(commitDetailsUrl, request.getAccessToken())
                        .bodyToFlux(CommitDetailsDiff.class)
                        .toStream().collect(Collectors.toList());
                for (CommitDetailsDiff commitDetailsDiff : commitDetails) {
                    if (commitDetailsDiff.getNew_path().endsWith(GitRepoConstants.FILE_EXTENSION)) {
                        featureFilePaths.add(commitDetailsDiff.getNew_path());
                    }
                }
            }
        }
        LOGGER.info("Total {} feature files found {}", featureFilePaths.size(), featureFilePaths);
        return featureFilePaths;
    }

    private InputStream getInputStream(String featureFilePath, String baseApiUrl, String projectId, String branch, String accessToken) throws Exception {
        String urlString = baseApiUrl + "/projects/" + projectId + "/repository/files/" + featureFilePath.replaceAll("/", "%2F") + "/raw?ref=" + branch;
        LOGGER.info("Reading file content of {} from url {}", featureFilePath, urlString);
        URL url = new URL(urlString);
        URLConnection uc = url.openConnection();
        uc.setRequestProperty("Private-Token", accessToken);
        return uc.getInputStream();
    }

    private GitLabInfoRequest validateGitLabRequest(GitLabInfoRequest request) {
        request.setAccessToken(
                InputValidatorUtil.addDefaultValueIfAccessTokenIsEmpty(request.getAccessToken()));
        gitlabRepoService.validateInputValues(request.getUserName(), request.getRepoUrl(),
                request.getAccessToken(), request.getBranch());
        request.setUserName(InputValidatorUtil.trimInputString(request.getUserName()));
        request.setRepoUrl(InputValidatorUtil.trimInputString(request.getRepoUrl()));
        request.setAccessToken(InputValidatorUtil.trimInputString(request.getAccessToken()));
        request.setBranch(InputValidatorUtil.trimInputString(request.getBranch()));

        gitlabRepoService.validateRepositoryUrl(request.getRepoUrl());

        request.setBaseApiUrl(gitlabRepoService.getBaseApiUrl(request.getRepoUrl()));

        request.setRepositoryPathName(InputValidatorUtil.extractRepositoryPathNameFromRepoUrl(request.getRepoUrl()));

        request.setRepositoryName(InputValidatorUtil.extractRepositoryNameFromRepoUrl(request.getRepoUrl()));

        return request;
    }

    private Set<String> extractAllFilesPaths(List<GitlabSearchResult> gitlabSearchResults) {
        return gitlabSearchResults.parallelStream()
                .map(GitlabSearchResult::getFilename)
                .collect(Collectors.toSet());
    }

    private ResponseSpec getWebClientResponse(String url, String accessToken) {
        return webClient.get()
                .uri(URI.create(url))
                .header("Private-Token", accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();

    }
}
