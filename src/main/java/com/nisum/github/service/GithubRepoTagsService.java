package com.nisum.github.service;

import com.nisum.domain.TagLinesDetails;
import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.github.domain.GithubAllBranchCommit;
import com.nisum.github.domain.GithubBranchDetails;
import com.nisum.github.domain.GithubBranchFileDetails;
import com.nisum.github.domain.GithubInfoRequest;
import com.nisum.github.domain.GithubMasterFilesDetails;
import com.nisum.github.domain.GithubMasterTagsResponse;
import com.nisum.github.domain.ParentGithubCommit;
import com.nisum.service.VersionControlService;
import com.nisum.util.GitRepoConstants;
import com.nisum.util.InputValidatorUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Profile({"dev", "qa"})
public class GithubRepoTagsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubRepoTagsService.class);

    @Autowired
    private GithubRepoService gitRepoService;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private WebClient webClient;

    @Value("${github.rawcontent.baseurl}")
    private String githubRawContentBaseUrl;

    @Value("${github.gapinc.rawcontent.baseurl}")
    private String gapincGithubRawContentBaseUrl;

    public List<TagLinesDetails> retrieveFeatureLinesForBuild(GithubInfoRequest request) throws Exception {
        request = this.validateGithubRequest(request);

        String rawContentApiBaseUrl = getRawContentApiUrl(request.getRepoUrl());

        request.setRepositoryName(InputValidatorUtil.extractRepositoryPathNameFromRepoUrl(request.getRepoUrl()));

        Map<String, String> featureFilePathsMap = this.getProjectFilesPath(request, rawContentApiBaseUrl);

        return this.retrieveFeatureFilesTags(request, featureFilePathsMap);
    }

    public Set<String> retrieveAllTags(GithubInfoRequest request) throws Exception {
        request = this.validateGithubRequest(request);

        String rawContentApiBaseUrl = getRawContentApiUrl(request.getRepoUrl());

        request.setRepositoryName(InputValidatorUtil.extractRepositoryPathNameFromRepoUrl(request.getRepoUrl()));

        Map<String, String> featureFilePathsMap = this.getProjectFilesPath(request, rawContentApiBaseUrl);

        Set<String> tags = new HashSet<>();
        for (Map.Entry<String, String> entry : featureFilePathsMap.entrySet()) {
            try {
                InputStream inputStream = this.getInputStream(entry, request.getUserName(), request.getAccessToken());
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
                LOGGER.info("This file {} might be removed from branch", entry.getKey());
            }
        }
        LOGGER.info("Tags with url are: {}", tags);
        return tags;
    }


    private Map<String, String> getProjectFilesPath(GithubInfoRequest request, String rawContentApiBaseUrl) throws Exception {
        Map<String, String> masterFilesPathURLMap = new HashMap<>();
        Map<String, String> branchFilesPathURLMap = new HashMap<>();
        if (StringUtils.equals(request.getBranch(), GitRepoConstants.MASTER)) {
            LOGGER.info("Getting master files paths with urls");
            GithubMasterTagsResponse masterTagsResponse = fetchMasterFilesTagsResponse(request, request.getBranch());

            enrichMasterTagsResponseWithDownloadURL(masterTagsResponse, request.getRepositoryName(), GitRepoConstants.MASTER, rawContentApiBaseUrl);

            masterFilesPathURLMap = retrieveMasterFilesWithDownloadUrlMap(masterTagsResponse);
        } else {
            LOGGER.info("getting branch commit details");
            Set<String> allCommitsFiles = fetchBranchFiles(request);

            LOGGER.info("Preparing branchFilesPath with rawcontentURL Map ");
            branchFilesPathURLMap = prepareBranchFilesWithURLMap(allCommitsFiles, request.getRepositoryName(), request.getBranch(), rawContentApiBaseUrl);
        }
        return Stream.of(masterFilesPathURLMap, branchFilesPathURLMap)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private String getRawContentApiUrl(String repositoryUrl) {
        if (StringUtils.containsIgnoreCase(repositoryUrl, "github.com/")) {
            return githubRawContentBaseUrl;
        } else if (StringUtils.containsIgnoreCase(repositoryUrl, "github.gapinc.com/")) {
            return gapincGithubRawContentBaseUrl;
        } else {
            throw new IncorrectInputException("this repository is not supported in this project");
        }
    }

    private Map<String, String> retrieveMasterFilesWithDownloadUrlMap(GithubMasterTagsResponse masterTagsResponse) {
        return masterTagsResponse.getItems()
                .parallelStream()
                .filter(e -> e.getName().endsWith(GitRepoConstants.FILE_EXTENSION))
                .collect(Collectors.toMap(
                        GithubMasterFilesDetails::getPath,
                        GithubMasterFilesDetails::getDownload_url
                ));
    }



    private List<TagLinesDetails> retrieveFeatureFilesTags(GithubInfoRequest request, Map<String, String> filesPathURLMap) throws Exception {
        Map<String, TagLinesDetails> fileTagsWithLinesMap = new HashMap<>();
        List<TagLinesDetails> tagLinesDetailsList = new ArrayList<>();
        List<String> tags = new ArrayList<>(Arrays.asList(request.getCommaSeparatedTags().split(",")));
        for (Map.Entry<String, String> entry : filesPathURLMap.entrySet()) {
            try {
                InputStream inputStream = this.getInputStream(entry, request.getUserName(), request.getAccessToken());

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
                                                if (!fileTagsWithLinesMap.containsKey(entry.getKey() + ":" + ordinal.get())) {
                                                    tagLinesDetailsList.add(new TagLinesDetails(tag, entry.getKey(), ordinal.get()));
                                                    fileTagsWithLinesMap.put(entry.getKey() + ":" + ordinal.get(), new TagLinesDetails(tag, entry.getKey(), ordinal.get()));
                                                }
                                            }
                                        });
                            }
                            ordinal.getAndIncrement();
                        });
            } catch (FileNotFoundException e) {
                LOGGER.info("This file {} might be removed from branch", entry.getKey());
            }
        }
        LOGGER.info("Files tags with url map size {} , data {}", tagLinesDetailsList.size(), tagLinesDetailsList);
        return tagLinesDetailsList;
    }

    public Set<String> retrieveTagsMatchedFeatureFiles(GithubInfoRequest request) throws Exception {
        request = this.validateGithubRequest(request);

        request.setCommaSeparatedTags(InputValidatorUtil.trimInputString(request.getCommaSeparatedTags()));

        String rawContentApiBaseUrl = getRawContentApiUrl(request.getRepoUrl());

        request.setRepositoryName(InputValidatorUtil.extractRepositoryPathNameFromRepoUrl(request.getRepoUrl()));

        LOGGER.info("fetching tags for user {}, repository {}, branch {} , tags {}",
                request.getUserName(), request.getRepositoryName(), request.getBranch(), request.getCommaSeparatedTags());


        Set<String> tags = convertSearchTagsToSet(request.getCommaSeparatedTags());

        Set<String> allFilesDetails = new HashSet<>();
        if (StringUtils.equals(request.getBranch(), GitRepoConstants.MASTER)) {
            LOGGER.info("getting files paths from master");
            GithubMasterTagsResponse masterTagsResponse = fetchMasterFilesTagsResponse(request, GitRepoConstants.MASTER);

            enrichMasterTagsResponseWithDownloadURL(masterTagsResponse, request.getRepositoryName(), request.getBranch(), rawContentApiBaseUrl);

            Map<String, String> masterFilesPathURLMap = retrieveMasterFilesWithDownloadUrlMap(masterTagsResponse);

            LOGGER.info("master files urls map size {}, elements {}  "
                    , masterFilesPathURLMap.size(), masterFilesPathURLMap);
            //Map<String, String> masterFilesPathURLMap = fetchMasterFilesPathURLMap(user, repository, branch, filesLanguage);

            LOGGER.debug("filtering matched tag files from master");
            allFilesDetails = captureFilesOfMatchedTags(masterFilesPathURLMap, tags, request.getRepositoryName(), request.getAccessToken());

        } else {
            LOGGER.info("Getting branch files for the given tags ");
            Set<String> allCommitsFiles = fetchBranchFiles(request);

            Map<String, String> branchFilesPathURLMap = prepareBranchFilesWithURLMap(allCommitsFiles, request.getRepositoryName(), request.getBranch(), rawContentApiBaseUrl);


            LOGGER.info("Filtering matched tag files branch");
            allFilesDetails = captureFilesOfMatchedTags(branchFilesPathURLMap, tags, request.getRepositoryName(), request.getAccessToken());


            LOGGER.info("Tags matched on all files  size {}, elements {} ", allFilesDetails.size(), allFilesDetails);
        }

        return allFilesDetails;
    }

    private Set<String> fetchBranchFiles(GithubInfoRequest request) throws Exception {
        LOGGER.info("Fetching git branch details user {} repository {} ,branch {}"
                , request.getUserName(), request.getRepositoryName(), request.getBranch());
        Flux<GithubBranchDetails> gitBranchDetailsFlux = gitRepoService.fetchGitBranchDetails(request, request.getBaseApiUrl());
        GithubAllBranchCommit masterCommitDetails = getMasterCommitDetails(gitBranchDetailsFlux);

        GithubBranchDetails gitBranch = gitRepoService.extractGitBranch(request.getBranch(), gitBranchDetailsFlux);

        List<ParentGithubCommit> parentGitCommits = null;
        boolean isMasterShaBranchShaMatched = false;
        String commitURL = gitBranch.getCommit().getUrl();
        List<GithubAllBranchCommit> allBranchCommits = new ArrayList<>();
        do {
            GithubAllBranchCommit allBranchCommit = gitRepoService
                    .fetchGitBranchCommits(commitURL, request.getUserName(), request.getAccessToken());
            parentGitCommits = allBranchCommit.getParents();
            if (CollectionUtils.isNotEmpty(parentGitCommits)) {
                commitURL = parentGitCommits.get(0).getUrl();
            }
            allBranchCommits.add(allBranchCommit);
            isMasterShaBranchShaMatched = StringUtils
                    .equalsIgnoreCase(masterCommitDetails.getSha(), allBranchCommit.getSha());
            if (isMasterShaBranchShaMatched) {
                LOGGER.info("master sha and branch sha matched");
                break;
            }
        } while (CollectionUtils.isNotEmpty(parentGitCommits));

        Set<String> masterFiles = null;
        if (isMasterShaBranchShaMatched && CollectionUtils.isNotEmpty(parentGitCommits)) {
            LOGGER.info("master branch sha matched. getting master files paths with urls");
            GithubMasterTagsResponse masterTagsResponse = fetchMasterFilesTagsResponse(request, GitRepoConstants.MASTER);
            masterFiles = masterTagsResponse.getItems()
                    .parallelStream()
                    .filter(e -> e.getName().endsWith(GitRepoConstants.FILE_EXTENSION))
                    .map(GithubMasterFilesDetails::getPath)
                    .collect(Collectors.toSet());
        }

        LOGGER.info("collecting branch commits files");
        Set<String> allCommitsFiles = new HashSet<>();
        allBranchCommits.parallelStream()
                .map(GithubAllBranchCommit::getFiles)
                .forEach(files -> {
                    Set<String> commitFiles = files.parallelStream()
                            .filter(file -> file.getFilename().endsWith(GitRepoConstants.FILE_EXTENSION))
                            .map(GithubBranchFileDetails::getFilename)
                            .collect(Collectors.toSet());
                    if (CollectionUtils.isNotEmpty(commitFiles)) {
                        allCommitsFiles.addAll(commitFiles);
                    }
                });
        if (CollectionUtils.isNotEmpty(masterFiles)) {
            allCommitsFiles.addAll(masterFiles);
        }
        LOGGER.info("git branch files {} found for branch {} ", allCommitsFiles.size(), request.getBranch());
        return allCommitsFiles;
    }

    private GithubAllBranchCommit getMasterCommitDetails(Flux<GithubBranchDetails> gitBranch) {
        return gitBranch.toStream()
                .filter(e -> StringUtils.equalsIgnoreCase(GitRepoConstants.MASTER, e.getName()))
                .map(e -> e.getCommit())
                .findFirst()
                .get();
    }

    private Set<String> convertSearchTagsToSet(String commaSeparatedTags) {
        return Stream.of(commaSeparatedTags.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private GithubMasterTagsResponse fetchMasterFilesTagsResponse(GithubInfoRequest request, String branchName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        String filesTagsUrl = request.getBaseApiUrl() + "/search/code?q=+language:" + request.getFilesLanguage() + "+repo:" + request.getRepositoryName();
        LOGGER.info("fetching master tags details for user {}, repository {} , branch {}, fileslanguage {}, filesTagsUrl {} ",
                request.getUserName(), request.getRepositoryName(), branchName, request.getFilesLanguage(), filesTagsUrl);
        GithubMasterTagsResponse masterTagsResponse = webClient.get()
                .uri(URI.create(filesTagsUrl))
                .header("Authorization", "Basic " + Base64Utils
                        .encodeToString((request.getRepositoryName() + ":" + request.getAccessToken())
                                .getBytes(StandardCharsets.UTF_8)))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(GithubMasterTagsResponse.class)
                .blockFirst();
        LOGGER.info("found {} files from master ", masterTagsResponse.getItems().size());

        return masterTagsResponse;

    }

    private void enrichMasterTagsResponseWithDownloadURL(GithubMasterTagsResponse branchTagsResponse,
                                                         String repositoryName, String branch, String rawContentApiBaseUrl) {
        branchTagsResponse.getItems()
                .forEach(e -> {
                    e.setDownload_url(prepareRawContentURL(repositoryName, branch, e.getPath(), rawContentApiBaseUrl));
                });
    }

    private Map<String, String> prepareBranchFilesWithURLMap(Set<String> filesPaths,
                                                             String repositoryName, String branch, String rawContentApiBaseUrl) {
        LOGGER.info("preparing {} files with raw content URLs", filesPaths.size());
        return filesPaths.parallelStream()
                .collect(Collectors.toMap(f -> f,
                        f -> prepareRawContentURL(repositoryName, branch, f, rawContentApiBaseUrl)));
    }

    private String prepareRawContentURL(String repositoryName,
                                        String branch, String filePath, String rawContentApiBaseUrl) {
        return new StringBuilder(rawContentApiBaseUrl)
                .append("/").append(repositoryName)
                .append("/").append(branch)
                .append("/").append(filePath)
                .toString();
    }

    private Set<String> captureFilesOfMatchedTags(Map<String, String> filesPathURLMap
            , Set<String> searchTags, String githubUser, String gitAccessToken) throws Exception {
        Set<String> tagsMatchedFiles = new HashSet<>();
        for (String tag : searchTags) {
            Set<String> tagFiles = new HashSet<>();
            for (Map.Entry<String, String> entry : filesPathURLMap.entrySet()) {
                try {
                    //InputStream inputStream = new URL(entry.getValue()).openStream();
                    URL url = new URL(entry.getValue());
                    URLConnection uc = url.openConnection();
                    String userpass = githubUser + ":" + gitAccessToken;
                    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
                    uc.setRequestProperty("Authorization", basicAuth);
                    InputStream inputStream = uc.getInputStream();
                    boolean isTagPresent =
                            IOUtils.readLines(inputStream, StandardCharsets.UTF_8)
                                    .parallelStream()
                                    .map(line -> line.trim())
                                    //.filter(line ->  StringUtils.startsWith(line, "@"))
                                    .anyMatch(line -> StringUtils.contains(line, tag));
                    if (isTagPresent) {
                        tagFiles.add(entry.getKey());
                    }
                } catch (FileNotFoundException e) {
                    LOGGER.info("This file {} might be removed from branch", entry.getKey());
                }
            }
            LOGGER.info("tag {} matched files {}", tag, tagFiles);
            if (CollectionUtils.isNotEmpty(tagFiles)) {
                tagsMatchedFiles.addAll(tagFiles);
            }
        }
        LOGGER.info("tags from all files : size {} , elements {} ", tagsMatchedFiles.size(), tagsMatchedFiles);
        return tagsMatchedFiles;
    }

    private GithubInfoRequest validateGithubRequest(GithubInfoRequest request) {
        request.setAccessToken(InputValidatorUtil.addDefaultValueIfAccessTokenIsEmpty(request.getAccessToken()));
        request.setFilesLanguage(StringUtils.isBlank(request.getFilesLanguage()) ? GitRepoConstants.FILES_LANGUAGE :
                InputValidatorUtil.trimInputString(request.getFilesLanguage()));
        gitRepoService.validateInputValues(request.getUserName(), request.getRepoUrl(), request.getBranch(), request.getAccessToken());

        request.setUserName(InputValidatorUtil.trimInputString(request.getUserName()));
        request.setRepoUrl(InputValidatorUtil.trimInputString(request.getRepoUrl()));
        request.setBranch(InputValidatorUtil.trimInputString(request.getBranch()));
        request.setAccessToken(InputValidatorUtil.trimInputString(request.getAccessToken()));
        request.setBaseApiUrl(gitRepoService.getBaseApiUrl(request.getRepoUrl()));

        return request;
    }

    private InputStream getInputStream(Map.Entry<String, String> entry, String githubUser, String accessToken) throws Exception {
        LOGGER.info("Reading file content of {}", entry.getKey());
        URL url = new URL(entry.getValue());
        URLConnection uc = url.openConnection();
        String userpass = githubUser + ":" + accessToken;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        uc.setRequestProperty("Authorization", basicAuth);
        InputStream inputStream = uc.getInputStream();
        return uc.getInputStream();
    }


}
