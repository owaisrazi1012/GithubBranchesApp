package com.nisum.github.service;

import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.github.domain.GithubAllBranchCommit;
import com.nisum.github.domain.GithubBranchDetails;
import com.nisum.github.domain.GithubBranchFileDetails;
import com.nisum.github.domain.GithubInfoRequest;
import com.nisum.github.domain.GithubRepoNameDetails;
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
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Profile({"dev", "qa"})
public class GithubRepoService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GithubRepoService.class);

	@Autowired
	private WebClient webClient;
	
	@Value("${github.baseapiurl}")
	private String githubBaseApiUrl;
	
	@Value("${github.gapinc.baseapiurl}")
	private String gapincGithubBaseApiUrl;

	public List<String> fetchBranches(GithubInfoRequest userInput) {
		userInput.setAccessToken(
				InputValidatorUtil.addDefaultValueIfAccessTokenIsEmpty(userInput.getAccessToken()));
		validateInputValues(userInput.getUserName(), userInput.getRepoUrl(), userInput.getAccessToken());
		
		userInput.setUserName(InputValidatorUtil.trimInputString(userInput.getUserName()));
		userInput.setRepoUrl(InputValidatorUtil.trimInputString(userInput.getRepoUrl()));
		userInput.setAccessToken(InputValidatorUtil.trimInputString(userInput.getAccessToken()));
		
		validateRepositoryUrl(userInput.getRepoUrl());
		
		String baseApiUrl = getBaseApiUrl(userInput.getRepoUrl());
		
		userInput.setRepositoryName(InputValidatorUtil.extractRepositoryPathNameFromRepoUrl(userInput.getRepoUrl()));
		
		LOGGER.info("fetching repository branches of user {}, repositoryUrl {}", userInput.getUserName(), userInput.getRepoUrl());
		Flux<GithubBranchDetails> gitBranchDetailsFlux = fetchGitBranchDetails(userInput, baseApiUrl);
		

		List<String> branchNames = gitBranchDetailsFlux.toStream()
		.map(GithubBranchDetails::getName)
		.collect(Collectors.toList());
		
		return branchNames;
	}
	
	protected String getBaseApiUrl(String repositoryUrl) {
		if(StringUtils.containsIgnoreCase(repositoryUrl, GitRepoConstants.GITHUB_URL_FORMAT)) {
			return githubBaseApiUrl;
		} else if (StringUtils.containsIgnoreCase(repositoryUrl, GitRepoConstants.GAPINC_GITHUB_URL_FORMAT)) {
			return gapincGithubBaseApiUrl;
		} else {
			throw new IncorrectInputException("this repository is not supported in this project");
		}
	}

	private void validateRepositoryUrl(String repositoryUrl) {
		if((!StringUtils.startsWith(repositoryUrl, "http") || !StringUtils.startsWith(repositoryUrl, "https"))
			&& !repositoryUrl.contains(".com/")) {
			throw new IncorrectInputException("repository URL format is not correct");
		}
	}

	protected Flux<GithubBranchDetails> fetchGitBranchDetails(GithubInfoRequest userInput, String baseApiUrl) {
		
		Flux<GithubBranchDetails> gitBranchDetailsFlux = webClient.get()
				.uri(URI.create(baseApiUrl + "/repos/"+userInput.getRepositoryName()+"/branches"))
				.header("Authorization", "Basic " + Base64Utils
	                    .encodeToString((userInput.getUserName() + ":" + userInput.getAccessToken()).getBytes(StandardCharsets.UTF_8)))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(GithubBranchDetails.class);
				
		return gitBranchDetailsFlux;
	}

	public Collection<String> retrieveBranchFiles(GithubInfoRequest userInput)
			throws URISyntaxException {
		userInput.setAccessToken(InputValidatorUtil.addDefaultValueIfAccessTokenIsEmpty(userInput.getAccessToken()));
		validateInputValues(userInput.getUserName(), userInput.getRepoUrl(), userInput.getBranch(),
				userInput.getAccessToken());
		
		userInput.setUserName(InputValidatorUtil.trimInputString(userInput.getUserName()));
		userInput.setRepoUrl(InputValidatorUtil.trimInputString(userInput.getRepoUrl()));
		userInput.setBranch(InputValidatorUtil.trimInputString(userInput.getBranch()));
		
		String baseApiUrl = getBaseApiUrl(userInput.getRepoUrl());
		
		userInput.setRepositoryName(InputValidatorUtil.extractRepositoryPathNameFromRepoUrl(userInput.getRepoUrl()));
		LOGGER.info("fetching tags for user {}, repository {}, branch {} ",
				userInput.getUserName(), userInput.getRepositoryName(), userInput.getBranch());
		
		Flux<GithubBranchDetails> gitBranchDetailsFlux = fetchGitBranchDetails(userInput, baseApiUrl);
		GithubBranchDetails gitBranch = extractGitBranch(userInput.getBranch(), gitBranchDetailsFlux);
			
		GithubAllBranchCommit commits = fetchGitBranchCommits(gitBranch.getCommit().getUrl(), userInput.getUserName(), userInput.getAccessToken());

		Map<String, String> branchFilesPathURLMap = fetchBranchFilesPathURLMap(commits);

		return branchFilesPathURLMap.keySet();
	}

	protected GithubBranchDetails extractGitBranch(final String branch, Flux<GithubBranchDetails> gitBranchDetailsFlux) {
		return gitBranchDetailsFlux
								.filter(e -> StringUtils.equalsIgnoreCase(branch, e.getName()))
								.blockFirst();
	}

	private Map<String, String> fetchBranchFilesPathURLMap(GithubAllBranchCommit commits) {
		Map<String, String> branchFilesPathURLMap = commits.getFiles()
				.parallelStream()
				.filter(e -> !StringUtils.equalsIgnoreCase(GitRepoConstants.FILE_STATUS_REMOVED, e.getStatus()))
				.filter(e -> e.getFilename().endsWith(GitRepoConstants.FILE_EXTENSION)) // filtering .feature files
				.collect(Collectors.toMap(GithubBranchFileDetails::getFilename, GithubBranchFileDetails::getRaw_url));
		LOGGER.info("branch files with urls map size {}, elemennts {}"
				, branchFilesPathURLMap.size(), branchFilesPathURLMap);
		return branchFilesPathURLMap;
	}
	
	protected GithubAllBranchCommit fetchGitBranchCommits(String commitUrl, String githubUsername, String gitAccessToken) throws URISyntaxException {
		LOGGER.info("fetching git branch commits for url {}", commitUrl);
		return webClient.get()
				.uri(new URI(commitUrl))
				.header("Authorization", "Basic " + Base64Utils
	                    .encodeToString((githubUsername + ":" + gitAccessToken).getBytes(StandardCharsets.UTF_8)))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(GithubAllBranchCommit.class)
				.blockFirst();
	}
	
	protected void validateInputValues(String... inputValues) {
		Arrays.asList(inputValues)
				.forEach(e ->  {
					if (InputValidatorUtil.isInputEmpty(e)) {
							throw new IncorrectInputException(GitRepoConstants.INPUT_FIELDS_EMPTY_MSG);
						}
					});
	}

	public Map<String, String> fetchRepoNameFullNameMap(String user, String gitAccessToken) {
		gitAccessToken = InputValidatorUtil.addDefaultValueIfAccessTokenIsEmpty(gitAccessToken);
		validateInputValues(user, gitAccessToken);
		user = InputValidatorUtil.trimInputString(user);
		gitAccessToken = InputValidatorUtil.trimInputString(gitAccessToken);

		Map<String, Object> params = new HashMap<>();
	    params.put("user", user);
	    LOGGER.info("fetching repositoies for user {}", user);
	    Map<String, String> repoNameFullNameMap = webClient.get()
	        .uri(githubBaseApiUrl+"/users/{user}/repos", params)
	        .header("Authorization", "Basic " + Base64Utils
                    .encodeToString((user + ":" + gitAccessToken).getBytes(StandardCharsets.UTF_8)))
	        .accept(MediaType.APPLICATION_JSON)
	        .retrieve()
	        .bodyToFlux(GithubRepoNameDetails.class)
	        .toStream()
	        .collect(Collectors.toMap(GithubRepoNameDetails::getName, GithubRepoNameDetails::getFull_name));
	    LOGGER.info("repository name with full name map size {}, elements {}"
	    		,repoNameFullNameMap.size(), repoNameFullNameMap);
		return repoNameFullNameMap;
	}

	public List<String> findTagFiles(String tag, Map<String, String> filesData, String githubUser, String gitAccessToken) throws Exception {
		List<String> tagFiles = new ArrayList<>();
		for (Map.Entry<String, String> entry : filesData.entrySet()) {

			URL url = new URL(entry.getKey());
			URLConnection uc;
			uc = url.openConnection();
			String userpass = githubUser + ":" + gitAccessToken;
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			uc.setRequestProperty ("Authorization", basicAuth);
			StringBuilder fileContent = null;

			uc.setRequestProperty("X-Requested-With", "Curl");
			BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line).append("\n");
			}
			if (fileContent.toString().contains(tag)) {
				tagFiles.add(entry.getValue());
			}
		}
		return tagFiles;
	}
	
}
