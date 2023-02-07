package com.nisum.gitlab.controller;

import com.nisum.domain.TagLinesDetails;
import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.gitlab.domain.GitLabInfoRequest;
import com.nisum.gitlab.service.GitlabRepoService;
import com.nisum.gitlab.service.GitlabRepoTagsService;
import com.nisum.exception.handling.EntityResponseFailure;
import com.nisum.exception.handling.EntityResponseSuccess;
import com.nisum.service.VersionControlService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/gitlab")
public class GitlabRepoController {

    @Autowired
    private GitlabRepoService gitlabRepoService;

    @Autowired
    private GitlabRepoTagsService gitlabRepoTagsService;

    @Autowired
    private VersionControlService versionControlService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabRepoController.class);

    /*
     * find all branches for
     * given gitlab_username , gitlab_repository_url and gitlab_accesstoken.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @PostMapping(value = "/branches")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> gitlabRepositoryBranches(@RequestBody GitLabInfoRequest request) {
        List<String> branchNamesDetails = null;
        LOGGER.info("Fetching branches {} ", request);
        try {
            branchNamesDetails = gitlabRepoService.fetchGitlabBranches(request);
            LOGGER.info("branch names{}", branchNamesDetails);

            return new EntityResponseSuccess<>(branchNamesDetails).getResponse();
        } catch (IncorrectInputException e) {
            LOGGER.error("Exception occurred when fetching branches ", e);
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        } catch (Exception e) {
            LOGGER.error("Error on retrieving branches ", e);
            return new EntityResponseFailure("Error on retrieving branches  " + e).getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the tag lines in Gitlab Repo for given tags to run parallel jenkins builds",
            description = "API for fetching all the tag lines in Gitlab Repo for given tags to run parallel jenkins builds"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @PostMapping(value = "/feature-tag-lines")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFeatureFilesTagsLine(@RequestBody GitLabInfoRequest request) {
        LOGGER.info("Getting Tag lines per build via tags");
        try {
            List<TagLinesDetails> tagLinesDetailsList = gitlabRepoTagsService.retrieveFeatureLinesForBuild(request);
            int tagLinesCount = tagLinesDetailsList.size();
            if (tagLinesCount >= request.getParallelBuilds()) {
                return new EntityResponseSuccess<>(versionControlService.prepareTagsWithFileNumbersMap(tagLinesDetailsList, tagLinesCount, request.getParallelBuilds()), "Tag lines per build Fetched Successfully").getResponse();
            } else {
                return new EntityResponseSuccess<>(new HashMap<>(), "Given Parallel Builds Count [" + request.getParallelBuilds() + "]is greater than Tag Lines Count [" + tagLinesCount + "]").getResponse();
            }
        } catch (IncorrectInputException e) {
            LOGGER.error("Exception occurred when getting Tag lines per build via tags : ", e);
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        } catch (Exception e) {
            LOGGER.error("Error on retrieving Tag lines per build via tags  ", e);
            return new EntityResponseFailure("Error on retrieving tags from all files " + e.getStackTrace()).getResponse();
        }
    }

    @Operation(
            summary = "API for fetching all the tags from all feature files in Gitlab Repository",
            description = "API for fetching all the tags from all feature files for given git lab repo, user and token"
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @PostMapping(value = "/tags")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllTags(@RequestBody GitLabInfoRequest request) {
        LOGGER.info("Getting all tags");
        try {
            return new EntityResponseSuccess<>(gitlabRepoTagsService.retrieveAllTags(request), "Tags Fetched Successfully").getResponse();
        } catch (IncorrectInputException e) {
            LOGGER.error("Exception occurred when getting all tags", e);
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        } catch (Exception e) {
            LOGGER.error("Error on retrieving tags from all files  ", e);
            return new EntityResponseFailure("Error on retrieving tags from all files " + e.getStackTrace()).getResponse();
        }
    }

}
