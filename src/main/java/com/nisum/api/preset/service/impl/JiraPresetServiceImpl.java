package com.nisum.api.preset.service.impl;

import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import com.nisum.api.preset.domain.dto.JiraPresetRequestDTO;
import com.nisum.api.preset.domain.dto.JiraPresetResponseDTO;
import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.exception.custom.JiraPresetServiceException;
import com.nisum.api.preset.repository.JiraPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.JiraPresetService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile({"dev", "qa"})
public class JiraPresetServiceImpl implements JiraPresetService {
    @Autowired
    private JiraPresetRepository jiraPresetRepository;

    @Autowired
    private TestPresetRepository testPresetRepository;

    @Override
    public Map<String, Object> getAllJiraPresets(int page, int size) throws JiraPresetServiceException {
        Page<JiraPreset> jiraPresetPage =
                jiraPresetRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        return getJiraPresetResponseMap(size, jiraPresetPage);
    }

    @Override
    public JiraPresetResponseDTO getJiraPresetDetails(Long id) throws JiraPresetServiceException {
        JiraPreset jiraPreset = jiraPresetRepository.findById(id)
                .orElseThrow(new JiraPresetServiceException(NOT_ACCEPTABLE.value(), NO_RECORD_FOUND, new Throwable()));
        return new JiraPresetResponseDTO(jiraPreset);
    }

    @Override
    public JiraPresetResponseDTO createJiraPreset(JiraPresetRequestDTO jiraPresetRequestDTO)
            throws JiraPresetServiceException {
        JiraPreset jiraPreset = jiraPresetRepository
                .save(JiraPreset.builder()
                        .userName(jiraPresetRequestDTO.getUserName())
                        .projectId(jiraPresetRequestDTO.getProjectId())
                        .token(jiraPresetRequestDTO.getToken())
                        .testPresets(jiraPresetRequestDTO.getTestPresetId()!= null ?
                                Collections.singleton(testPresetRepository.findById(jiraPresetRequestDTO.getTestPresetId()).get()) : null)
                        .build());
        return new JiraPresetResponseDTO(jiraPreset);
    }

    @Override
    public JiraPresetResponseDTO updateJiraPreset(Long id, JiraPresetRequestDTO jiraPresetRequestDTO)
            throws JiraPresetServiceException {

        JiraPreset jiraPreset = jiraPresetRepository.findById(id)
                .orElseThrow(new JiraPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable()));

        jiraPreset.setUserName(jiraPresetRequestDTO.getUserName());
        jiraPreset.setProjectId(jiraPresetRequestDTO.getProjectId());
        jiraPreset.setToken(jiraPresetRequestDTO.getToken());
        if (jiraPresetRequestDTO.getTestPresetId() != null) {
            jiraPreset.setTestPresets(Collections.singleton(testPresetRepository.findById(jiraPresetRequestDTO.getTestPresetId()).get()));
        }
        return new JiraPresetResponseDTO(jiraPresetRepository.save(jiraPreset));
    }

    @Override
    public void deleteJiraPreset(Long id) throws JiraPresetServiceException {
        try {
            jiraPresetRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new JiraPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
        }
    }
    private Map<String, Object> getJiraPresetResponseMap(int size, Page<JiraPreset> jiraPresetPage) {
        Map<String, Object> response = new HashMap<>();
        if (jiraPresetPage.hasContent()) {
            response.put("jiraPresets",
                    jiraPresetPage.getContent().stream()
                            .map(JiraPresetResponseDTO::new)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
            response.put("currentPage",  jiraPresetPage.getNumber());
            response.put("limit",  size);
            response.put("totalRecords",  jiraPresetPage.getTotalElements());
            response.put("totalPages",  jiraPresetPage.getTotalPages());
        } else { //populate empty map
            response.put("jiraPresets",  new HashSet<>());
            response.put("currentPage",  0);
            response.put("limit",  size);
            response.put("totalRecords",  0l);
            response.put("totalPages",  0);
        }
        return response;
    }
}
