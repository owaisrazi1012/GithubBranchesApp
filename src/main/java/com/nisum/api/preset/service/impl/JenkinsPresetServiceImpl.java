package com.nisum.api.preset.service.impl;

import com.nisum.api.preset.domain.dto.JenkinsPresetRequestDTO;
import com.nisum.api.preset.domain.dto.JenkinsPresetResponseDTO;
import com.nisum.api.preset.domain.entity.JenkinsPreset;
import com.nisum.exception.custom.JenkinsPresetServiceException;
import com.nisum.api.preset.repository.JenkinsPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.JenkinsPresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
@Profile({"dev", "qa"})
public class JenkinsPresetServiceImpl implements JenkinsPresetService {


    @Autowired
    private JenkinsPresetRepository jenkinsPresetRepository;

    @Autowired
    private TestPresetRepository testPresetRepository;

    @Override
    public Map<String, Object> getAllJenkinsPresets(int page, int size) {
        Page<JenkinsPreset> jenkinsPresetPage =
                jenkinsPresetRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        return getJenkinsPresetResponseMap(size, jenkinsPresetPage);
    }

    @Override
    public JenkinsPresetResponseDTO getJenkinsPresetDetails(Long id) {
        JenkinsPreset jenkinsPreset = jenkinsPresetRepository.findById(id)
                .orElseThrow(new JenkinsPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable()));
        return new JenkinsPresetResponseDTO(jenkinsPreset);
    }

    @Override
    public JenkinsPresetResponseDTO createJenkinsPreset(@RequestBody JenkinsPresetRequestDTO jenkinsPresetRequest) {

        JenkinsPreset newJenkinsPreset = jenkinsPresetRepository
                .save(JenkinsPreset.builder()
                        .name(jenkinsPresetRequest.getName())
                        .url(jenkinsPresetRequest.getUrl())
                        .userName(jenkinsPresetRequest.getUserName())
                        .password(jenkinsPresetRequest.getPassword())
                        .slave(jenkinsPresetRequest.getSlave())
                        .credentialId(jenkinsPresetRequest.getCredentialId())
                        .testPresets(jenkinsPresetRequest.getTestPresetId() != null ?
                                Collections.singleton(testPresetRepository.findById(jenkinsPresetRequest.getTestPresetId()).get()) : null)
                        .build());

        return new JenkinsPresetResponseDTO(newJenkinsPreset);
    }

    @Override
    public JenkinsPresetResponseDTO updateJenkinsPreset(Long id, JenkinsPresetRequestDTO jenkinsPresetRequest) {

        JenkinsPreset jenkinsPreset = jenkinsPresetRepository.findById(id)
                .orElseThrow(new JenkinsPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable()));

        jenkinsPreset.setName(jenkinsPresetRequest.getName());
        jenkinsPreset.setUserName(jenkinsPresetRequest.getUserName());
        jenkinsPreset.setPassword(jenkinsPresetRequest.getPassword());
        jenkinsPreset.setCredentialId(jenkinsPresetRequest.getCredentialId());
        jenkinsPreset.setSlave(jenkinsPresetRequest.getSlave());
        jenkinsPreset.setUrl(jenkinsPresetRequest.getUrl());
        if (jenkinsPresetRequest.getTestPresetId() != null) {
            jenkinsPreset.getTestPresets().add(testPresetRepository.findById(jenkinsPresetRequest.getTestPresetId()).get());
        }

        JenkinsPreset updatedJenkinsPreset = jenkinsPresetRepository.save(jenkinsPreset);
        return new JenkinsPresetResponseDTO(updatedJenkinsPreset);
    }

    @Override
    public void deleteJenkinsPreset(Long id) {
        try {
            jenkinsPresetRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new JenkinsPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
        }
    }

    private Map<String, Object> getJenkinsPresetResponseMap(int size, Page<JenkinsPreset> jenkinsPresetPage) {
        Map<String, Object> response = new HashMap<>();
        if (jenkinsPresetPage.hasContent()) {
            response.put("jenkinsPresets",  jenkinsPresetPage.getContent().stream()
                    .map(JenkinsPresetResponseDTO::new)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            response.put("currentPage",  jenkinsPresetPage.getNumber());
            response.put("limit",  size);
            response.put("totalRecords",  jenkinsPresetPage.getTotalElements());
            response.put("totalPages",  jenkinsPresetPage.getTotalPages());
        } else { // populate empty map
            response.put("jenkinsPresets",  new HashSet<>());
            response.put("currentPage",  0);
            response.put("limit",  size);
            response.put("totalRecords",  0l);
            response.put("totalPages",  0);
        }
        return response;
    }
}
