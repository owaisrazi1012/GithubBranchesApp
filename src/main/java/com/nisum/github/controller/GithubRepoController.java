package com.nisum.github.controller;

import com.nisum.domain.TagLinesDetails;
import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.github.domain.GithubInfoRequest;
import com.nisum.github.service.GithubRepoService;
import com.nisum.github.service.GithubRepoTagsService;
import com.nisum.service.VersionControlService;
import com.nisum.exception.handling.EntityResponseFailure;
import com.nisum.exception.handling.EntityResponseSuccess;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/github")
public class GithubRepoController {

	@Autowired
	private GithubRepoService githubRepoService;
	
	@Autowired
	private GithubRepoTagsService gitRepoTagsService;

	@Autowired
	private VersionControlService versionControlService;

	private static final Logger LOGGER = LoggerFactory.getLogger(GithubRepoController.class);
	
	/*
	 * find all branches for 
	 * given git_username and git_repository_url and github_accesstoken.
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
	@PostMapping(value = "/branches")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> githubRepositoryBranches(@RequestBody GithubInfoRequest request) {
		List<String> branchNamesDetails = null;
		LOGGER.info("Finding branches");
		try {
			branchNamesDetails = githubRepoService.fetchBranches(request);
			LOGGER.info("branch names with URLs  {}",branchNamesDetails);
			
			return new EntityResponseSuccess<>(branchNamesDetails).getResponse();
		} catch (IncorrectInputException e) {
			LOGGER.error("Exception occurred {}", e.getMessage());
			return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
		} catch (Exception e) {
			LOGGER.error("Error on retrieving branches {} ",  e.getMessage());
			return new EntityResponseFailure("error on retrieving branches  " + e.getMessage()).getResponse();
		}
	}
	
	/*
	 * find all matched .feature files for
	 * given git_username , git_repository_url, branch_name, feature files tags (comma separated tags)
	 * and github_accesstoken
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
	@PostMapping(value = "/featurefiles")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getFeatureFilesForTags(@RequestBody GithubInfoRequest request) {
		Set<String> tagsMatchedFeatureFiles = null;
		LOGGER.info("getting all features files for given tags");
		try {
			tagsMatchedFeatureFiles = gitRepoTagsService.retrieveTagsMatchedFeatureFiles(request);
			return new EntityResponseSuccess<>(tagsMatchedFeatureFiles, "Tags Fetched Successfully").getResponse();
		} catch (IncorrectInputException e) {
			LOGGER.error("Exception occurred {}", e.getMessage());
			return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
		} catch (Exception e) {
			LOGGER.error("Error on retrieving tags files {} ",  e.getMessage());
			return new EntityResponseFailure("error on retrieving tags files " + e.getMessage()).getResponse();
		}
	}


	@Operation(
			summary = "API for fetching all the tag lines in Github Repo for given tags to run parallel jenkins builds",
			description = "API for fetching all the tag lines in Github Repo for given tags to run parallel jenkins builds"
	)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
	@PostMapping(value = "/feature-tag-lines")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getFeatureFilesTagsLine(@RequestBody GithubInfoRequest request) {
		LOGGER.info("Getting Tag lines per build via tags");
		try {
			List<TagLinesDetails> tagLinesDetailsList = gitRepoTagsService.retrieveFeatureLinesForBuild(request);
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
			summary = "API for fetching all the tags from all feature files in Github Repository",
			description = "API for fetching all the tags from all feature files for given github repo, user and token"
	)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @PostMapping(value = "/tags")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getAllTags(@RequestBody GithubInfoRequest request) {
		LOGGER.info("Getting all tags");
		try {
			return new EntityResponseSuccess<>(gitRepoTagsService.retrieveAllTags(request), "Tags Fetched Successfully").getResponse();
		} catch (IncorrectInputException e) {
			LOGGER.error("Exception occurred when getting all tags", e);
			return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
		} catch (Exception e) {
			LOGGER.error("Error on retrieving tags from all files  ", e);
			return new EntityResponseFailure("Error on retrieving tags from all files " + e.getStackTrace()).getResponse();
		}
	}
	
	//This is unused method
	/*
	 * Pull all Repositories for github_username and github_accesstoken.
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
	@GetMapping(value = "/user/{user}/repos")
	public ResponseEntity<Map<String, Object>> getRepositoriesByUser(@PathVariable(name = "user") String user,
			@RequestParam(name = "accessToken", required=false) String gitAccessToken) {
		Map<String, String> repoNameFullNameMap = null;
		LOGGER.info("Finding repositories");
	    try {
	    	repoNameFullNameMap = githubRepoService.fetchRepoNameFullNameMap(user, gitAccessToken);
			return new EntityResponseSuccess<>(repoNameFullNameMap).getResponse();
		} catch (IncorrectInputException e) {
			LOGGER.error("Exception occurred {}", e.getMessage());
			return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
		} catch (Exception e) {
			LOGGER.error("Error on retrieving repositories {} ",  e.getMessage());
			return new EntityResponseFailure("error on retrieving repositories " + e.getMessage()).getResponse();
		}
	}
	
	//This is unused method
	/*
	 * find all matched .feature files for
	 * given git_username , git_repository_url, branch_name
	 * and github_accesstoken
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @PostMapping(value = "/all-featurefiles")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getAllFeatureFiles(@RequestBody GithubInfoRequest request) {
		Collection<String> branchFilesPaths = null;
		LOGGER.info("getting branch files");
		try {
			branchFilesPaths = githubRepoService.retrieveBranchFiles(request);
			return new EntityResponseSuccess<>(branchFilesPaths).getResponse();
		} catch (IncorrectInputException e) {
			LOGGER.error("Exception occurred {}", e.getMessage());
			return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
		} catch (Exception e) {
			LOGGER.error("Error on retrieving branch files {} ",  e.getMessage());
			return new EntityResponseFailure("error on retrieving branch files " + e.getMessage()).getResponse();
		}
	}
}
