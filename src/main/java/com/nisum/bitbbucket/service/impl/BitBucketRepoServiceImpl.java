package com.nisum.bitbbucket.service.impl;

import com.nisum.bitbbucket.domain.BitBucketBranches;
import com.nisum.bitbbucket.domain.BitBucketBranchesApiResponse;
import com.nisum.bitbbucket.domain.BitBucketFetchTagsApiResponse;
import com.nisum.bitbbucket.domain.BitBucketInfoRequest;
import com.nisum.bitbbucket.domain.BitBucketTagLinesDetails;
import com.nisum.bitbbucket.domain.BranchCommitDetails;
import com.nisum.bitbbucket.service.BitBucketService;
import com.nisum.domain.TagLinesDetails;
import com.nisum.exception.custom.IncorrectInputException;
import com.nisum.service.VersionControlService;
import com.nisum.util.Constants;
import com.nisum.util.InputValidatorUtil;
import org.apache.commons.io.IOUtils;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.nisum.util.Constants.AUTHORIZATION;
import static com.nisum.util.Constants.BEARER;

@Service
@Profile({"dev", "qa"})
public class BitBucketRepoServiceImpl implements BitBucketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitBucketRepoServiceImpl.class);
    @Autowired
    private WebClient webClient;

    @Autowired
    VersionControlService versionControlService;

    @Value("${bitbucket.nisum.baseUri}")
    private String bitbucketBaseApiUrl;

    @Override
    public Set<String> loadBranches(BitBucketInfoRequest request) {
        request = this.validateBitBucketInfoRequest(request);

        Flux<BitBucketBranchesApiResponse> gitBranchDetailsFlux = fetchGitBranchDetails(request.getAccessToken(), request.getUserName(), request.getRepositoryName(), null);

        return gitBranchDetailsFlux.toStream()
                .flatMap(list -> list.getValues().stream().map(BitBucketBranches::getName))
                .collect(Collectors.toSet());

    }

    @Override
    public Set<String> retrieveAllTags(BitBucketInfoRequest request) throws Exception {
        validateInputValues(request.getBranch());
        request = this.validateBitBucketInfoRequest(request);
        request.setBranch(InputValidatorUtil.trimInputString(request.getBranch()));
        Flux<BitBucketFetchTagsApiResponse> gitTagFlux = retrieveFeatureFiles(request.getAccessToken(), request.getUserName(), request.getRepositoryName(), request.getBranch());

        Set<String> featureFiles = gitTagFlux.toStream()
                .flatMap(list -> list.getValues().stream().map(BranchCommitDetails::getPath))
                .collect(Collectors.toSet());

        Set<String> tags = new HashSet<>();
        for (String fileName : featureFiles) {
            try {
                InputStream inputStream = this.getInputStream(fileName, request.getAccessToken(), request.getUserName(), request.getRepositoryName(), request.getBranch());

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
                LOGGER.info("This file {} might be removed from branch", fileName);
            }
        }
        LOGGER.info("Tags with url are: {}", tags);
        return tags;
    }

    @Override
    public List<TagLinesDetails> retrieveFeatureLinesForBuild(BitBucketInfoRequest request) throws Exception {
        validateInputValues(request.getBranch());
        request = this.validateBitBucketInfoRequest(request);
        request.setBranch(InputValidatorUtil.trimInputString(request.getBranch()));
        Flux<BitBucketFetchTagsApiResponse> gitTagFlux = retrieveFeatureFiles(request.getAccessToken(), request.getUserName(), request.getRepositoryName(), request.getBranch());

        Set<String> featureFiles = gitTagFlux.toStream()
                .flatMap(list -> list.getValues().stream().map(BranchCommitDetails::getPath))
                .collect(Collectors.toSet());

        return this.retrieveFeatureFilesTags(request, featureFiles);
    }

    private Flux<BitBucketBranchesApiResponse> fetchGitBranchDetails(String accessToken, String workspaceId, String repoName, String branchName) {

        String q = branchName != null ? "&q=name=%22" + branchName + "%22" : "";
        return webClient.get()
                .uri(URI.create(bitbucketBaseApiUrl + workspaceId + "/" + repoName + "/refs/branches?fields=values.name" + q))
                .header(AUTHORIZATION, BEARER + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(BitBucketBranchesApiResponse.class);

    }

    private Flux<BitBucketFetchTagsApiResponse> retrieveFeatureFiles(String accessToken, String workspaceId, String repoName, String branchName) {

        return webClient.get()
                .uri(URI.create(bitbucketBaseApiUrl + workspaceId + "/" + repoName + "/src/" + branchName +
                        "/features?fields=values.path&q=path~%22.feature%22"))
                .header(AUTHORIZATION, BEARER + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(BitBucketFetchTagsApiResponse.class);

    }

    private List<TagLinesDetails> retrieveFeatureFilesTags(BitBucketInfoRequest request, Set<String> fileNames) throws Exception {
        Map<String, TagLinesDetails> fileTagsWithLinesMap = new HashMap<>();
        List<TagLinesDetails> tagLinesDetailsList = new ArrayList<>();
        List<String> tags = new ArrayList<>(Arrays.asList(request.getCommaSeparatedTags().split(",")));
        for (String fileName : fileNames) {
            try {
                InputStream inputStream = this.getInputStream(fileName, request.getAccessToken(), request.getUserName(), request.getRepositoryName(), request.getBranch());

                AtomicInteger ordinal = new AtomicInteger(2);
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
                                                if (!fileTagsWithLinesMap.containsKey(fileName + ":" + ordinal.get())) {
                                                    tagLinesDetailsList.add(new TagLinesDetails(tag, fileName, ordinal.get()));
                                                    fileTagsWithLinesMap.put(fileName + ":" + ordinal.get(), new TagLinesDetails(tag, fileName, ordinal.get()));
                                                }
                                            }
                                        });
                            }
                            ordinal.getAndIncrement();
                        });
                LOGGER.debug("after reading file content of file {} , tagslinenumbers map {}", fileName, fileTagsWithLinesMap);

            } catch(IOException e) {
                LOGGER.info("This file {} might be removed from branch",fileName);
            }
        }
        LOGGER.info("Files tags with url map size {} , data {}", tagLinesDetailsList.size(), tagLinesDetailsList);
        return tagLinesDetailsList;
    }

    private Map<String, Set<String>> prepareTagsWithFileNumbersMap(
            Map<String, BitBucketTagLinesDetails> fileTagsWithLinesMap) {
        LOGGER.info("preparing consolidated map of tags with filesnumbers");
        Map<String, Set<String>> tagsFileNumbers = new HashMap<>();
        fileTagsWithLinesMap.forEach((key, value) -> {
            String tag = StringUtils.substringBefore(key, "-");
            String lineNumbers = StringUtils.join(value.getLineNumbers(), ":");
            if (tagsFileNumbers.containsKey(tag)) {
                Set<String> files = tagsFileNumbers.get(tag);
                files.add(value.getFilePath() + ":" + lineNumbers);
                tagsFileNumbers.put(tag, files);
            } else {
                Set<String> files = new LinkedHashSet<>();
                files.add(value.getFilePath() + ":" + lineNumbers);
                tagsFileNumbers.put(tag, files);
            }

        });
        LOGGER.info("tags with filesnumbers {}", tagsFileNumbers);
        return tagsFileNumbers;
    }

    protected void validateInputValues(String... inputValues) {
        Arrays.asList(inputValues).forEach(e -> {
            if (InputValidatorUtil.isInputEmpty(e)) {
                throw new IncorrectInputException(Constants.INPUT_FIELDS_EMPTY_MSG);
            }
        });
    }

    private BitBucketInfoRequest validateBitBucketInfoRequest(BitBucketInfoRequest request) {
        validateInputValues(request.getAccessToken(), request.getRepoUrl(),
                request.getUserName());
        request.setRepoUrl(InputValidatorUtil.trimInputString(request.getRepoUrl()));
        request.setUserName(InputValidatorUtil.trimInputString(request.getUserName()));
        request.setAccessToken(InputValidatorUtil.trimInputString(request.getAccessToken()));
        request.setRepositoryName(InputValidatorUtil.extractRepositoryNameFromRepoUrl(request.getRepoUrl()));
        return request;
    }

    private InputStream getInputStream(String fileName, String accessToken, String workspaceId, String repoName, String branchName) throws Exception {
        LOGGER.info("Reading file content of {}", fileName);
        URL url = new URL(bitbucketBaseApiUrl + workspaceId + "/" + repoName + "/src/" + branchName + "/" + fileName);
        URLConnection uc = url.openConnection();
        String auth = "Bearer " + accessToken;
        uc.setRequestProperty("Authorization", auth);
        return uc.getInputStream();
    }

}
