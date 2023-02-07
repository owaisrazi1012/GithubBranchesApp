package com.nisum.util;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class InputValidatorUtil {

	private InputValidatorUtil() {
	}

	public static boolean isInputEmpty(String input) {
		return StringUtils.isBlank(input);
	}

	public static String trimInputString(String input) {
		return input.trim();
	}

	public static String addDefaultValueIfAccessTokenIsEmpty(String gitAccessToken) {
		return StringUtils.isBlank(gitAccessToken) ? GitRepoConstants.DEFAULT_GIT_ACCESS_TOKEN : gitAccessToken;
		
	}
	
	public static String extractRepositoryPathNameFromRepoUrl(String repositoryUrl) {
		return StringUtils.substringAfter(repositoryUrl, ".com/");
	}

	public static String extractRepositoryNameFromRepoUrl(String repositoryUrl) {
		return  repositoryUrl.substring(repositoryUrl.lastIndexOf("/")+1);
	}
	
	public static AtomicInteger getFeatureFileInitialLineNumber() {
		return new AtomicInteger(2);
	}

}