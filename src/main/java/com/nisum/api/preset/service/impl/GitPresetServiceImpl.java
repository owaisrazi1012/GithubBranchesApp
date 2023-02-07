package com.nisum.api.preset.service.impl;

import com.nisum.api.preset.domain.dto.GitPresetRequestDTO;
import com.nisum.api.preset.domain.dto.GitPresetResponseDTO;
import com.nisum.api.preset.domain.entity.GitPreset;
import com.nisum.exception.custom.GitPresetServiceException;
import com.nisum.api.preset.repository.GitPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.GitPresetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
@Slf4j
@Profile({"dev", "qa"})
public class GitPresetServiceImpl implements GitPresetService {
    @Autowired
   private  GitPresetRepository gitPresetRepository;

    @Autowired
    private TestPresetRepository testPresetRepository;

    @Override
    public Map<String, Object> getAllGitPresets(int page, int size)  {

        Page<GitPreset> gitPresetPage = gitPresetRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        return getGitPresetResponseMap(size, gitPresetPage);
    }

    @Override
    public GitPresetResponseDTO createGitPreset(GitPresetRequestDTO gitPresetRequest) throws GitPresetServiceException {
        GitPreset gitPreset = gitPresetRepository
                .save(GitPreset.builder()
                        .name(gitPresetRequest.getName())
                        .repoUrl(gitPresetRequest.getRepoUrl())
                        .userName(gitPresetRequest.getUserName())
                        .accessToken(gitPresetRequest.getAccessToken())
                        .branch(gitPresetRequest.getBranch())
                        .testPresets(gitPresetRequest.getTestPresetId()!= null ?
                                Collections.singleton(testPresetRepository.findById(gitPresetRequest.getTestPresetId()).get()) : null)
                        .build());
        return new GitPresetResponseDTO(gitPreset);
    }

    @Override
    public GitPresetResponseDTO getGitPresetDetails(Long id)  throws GitPresetServiceException {
            Optional<GitPreset> gitPreset = gitPresetRepository.findById(id);
             if(gitPreset.isPresent()) {
                  return gitPreset.map(GitPresetResponseDTO::new).get();
             }
             else throw new GitPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
    }

    @Override
    public GitPresetResponseDTO  updateGitPreset(Long id, GitPresetRequestDTO gitPresetRequest) {


            Optional<GitPreset> gitPresetData = gitPresetRepository.findById(id);

            if (gitPresetData.isPresent()) {
                GitPreset gitPreset = gitPresetData.get();
                gitPreset.setName(gitPresetRequest.getName());
                gitPreset.setRepoUrl(gitPresetRequest.getRepoUrl());
                gitPreset.setUserName(gitPresetRequest.getUserName());
                gitPreset.setAccessToken(gitPresetRequest.getAccessToken());
                gitPreset.setBranch(gitPresetRequest.getBranch());
                if (gitPresetRequest.getTestPresetId() != null) {
                    gitPreset.setTestPresets(Collections.singleton(
                            testPresetRepository.findById(gitPresetRequest.getTestPresetId()).get()));
                }
                return new GitPresetResponseDTO(gitPresetRepository.save(gitPreset));
            }
            else throw new GitPresetServiceException(NOT_ACCEPTABLE.value(), NO_RECORD_FOUND, new Throwable());

    }

    @Override
    public void deleteGitPreset(Long id)  throws GitPresetServiceException {
        try {
            gitPresetRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new GitPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
        }
    }

    private Map<String, Object> getGitPresetResponseMap(int size, Page<GitPreset> gitPresetPage) {
            Map<String, Object> response = new HashMap<>();
            if(gitPresetPage.hasContent()) {
                response.put("gitPresets",  gitPresetPage.getContent().stream()
                        .map(GitPresetResponseDTO::new)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
                response.put("currentPage",  gitPresetPage.getNumber());
                response.put("limit",  size);
                response.put("totalRecords",  gitPresetPage.getTotalElements());
                response.put("totalPages",  gitPresetPage.getTotalPages());
            } else { //populate empty map
                response.put("gitPresets",  new HashSet<>());
                response.put("currentPage",  0);
                response.put("limit",  size);
                response.put("totalRecords",  0L);
                response.put("totalPages",  0);
            }
            return response;
    }
}
