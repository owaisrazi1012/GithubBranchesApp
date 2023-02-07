package com.nisum.api.preset.service.impl;

import com.nisum.api.preset.domain.dto.TestPresetRequestDTO;
import com.nisum.api.preset.domain.dto.TestPresetResponseDTO;
import com.nisum.api.preset.domain.entity.JenkinsPreset;
import com.nisum.api.preset.domain.entity.TestPreset;

import com.nisum.exception.custom.TestingPresetServiceException;
import com.nisum.api.preset.repository.ExecutionPresetRepository;
import com.nisum.api.preset.repository.GitPresetRepository;
import com.nisum.api.preset.repository.JenkinsPresetRepository;
import com.nisum.api.preset.repository.JiraPresetRepository;
import com.nisum.api.preset.repository.NotificationPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.PresetMapperService;
import com.nisum.api.preset.service.TestingPresetService;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service
@Profile({"dev", "qa"})
public class TestingPresetServiceImpl implements TestingPresetService {

    @Autowired
    private TestPresetRepository testPresetRepository;

    @Autowired
    private GitPresetRepository gitPresetRepository;

    @Autowired
    private JenkinsPresetRepository jenkinsPresetRepository;

    @Autowired
    private ExecutionPresetRepository executionPresetRepository;

    @Autowired
    private NotificationPresetRepository notificationPresetRepository;

    @Autowired
    private JiraPresetRepository jiraPresetRepository;


    @Autowired
    private PresetMapperService presetMapperService;

    @Override
    public Map<String, Object> getAllTestPresets(int page, int size) {
        Page<TestPreset> testPresetsPage = testPresetRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));

        return getTestPresetServiceResponseMap(size, testPresetsPage);
    }

    @Override
    public Page<TestPreset> getAllTestPresetsPage(int page, int size) {
        return testPresetRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Override
    public List<String> getAllTestPresetsName() {
        List<String> testPresetName = new ArrayList<>();
        List<TestPreset> testPresets = testPresetRepository.findAllByOrderByIdDesc();
        if (testPresets != null && !testPresets.isEmpty()) {
            testPresetName = testPresets.stream().map(tp -> tp.getName()).collect(Collectors.toList());
        }
        return testPresetName;
    }

    @Override
    public TestPresetResponseDTO getTestPresetDetails(Long id) {

        Optional<TestPreset> testPreset = testPresetRepository.findById(id);


        if(!testPreset.isPresent())
            throw new TestingPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());

        return presetMapperService.mapTestPresetToDTO(testPreset.get());

    }

    @Override
    public TestPresetResponseDTO createTestPreset(TestPresetRequestDTO testPresetRequest) {
        TestPreset newTestPreset = testPresetRepository.save(TestPreset.builder()
                .name(testPresetRequest.getName())
                .tags(testPresetRequest.getTags())
                .parallelBuilds(testPresetRequest.getParallelBuilds())
                .gitPreset(testPresetRequest.getGitPresetId() != null ? gitPresetRepository.findById(testPresetRequest.getGitPresetId()).get() : gitPresetRepository.findById(testPresetRequest.getGitPresetId()).orElse(null
                ))
                .jenkinsPreset(testPresetRequest.getJenkinsPresetId() != null ? jenkinsPresetRepository.findById(testPresetRequest.getJenkinsPresetId()).get() : null)
                .executionPreset(testPresetRequest.getExecutionPresetId() != null ? executionPresetRepository.findById(testPresetRequest.getExecutionPresetId()).get() : null)
                .notificationPreset(testPresetRequest.getNotificationPresetId() != null ? notificationPresetRepository.findById(testPresetRequest.getNotificationPresetId()).get() : null)
                .jiraPreset(testPresetRequest.getJiraPresetId() != null ? jiraPresetRepository.findById(testPresetRequest.getJiraPresetId()).get() : null)
                .build());
        return presetMapperService.mapTestPresetToDTO(newTestPreset);

    }

    @Override
    public TestPresetResponseDTO updateTestPreset(Long id, TestPresetRequestDTO testPresetRequest) {
        TestPreset testPreset = testPresetRepository.findById(id)
                .orElseThrow(new TestingPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable()) );


        testPreset.setName(testPresetRequest.getName());
        testPreset.setTags(testPresetRequest.getTags());
        testPreset.setParallelBuilds(testPresetRequest.getParallelBuilds());
        testPreset.setGitPreset(testPresetRequest.getGitPresetId() != null ? gitPresetRepository.findById(testPresetRequest.getGitPresetId()).get() : null);
        testPreset.setJenkinsPreset(testPresetRequest.getJenkinsPresetId() != null ? jenkinsPresetRepository.findById(testPresetRequest.getJenkinsPresetId()).get() : null);
        testPreset.setExecutionPreset(testPresetRequest.getExecutionPresetId() != null ? executionPresetRepository.findById(testPresetRequest.getExecutionPresetId()).get() : null);
        testPreset.setNotificationPreset(testPresetRequest.getNotificationPresetId() != null ? notificationPresetRepository.findById(testPresetRequest.getNotificationPresetId()).get() : null);
        testPreset.setJiraPreset(testPresetRequest.getJiraPresetId() != null ? jiraPresetRepository.findById(testPresetRequest.getJiraPresetId()).get() : null);

        return presetMapperService.mapTestPresetToTestPresetDTO(testPresetRepository.save(testPreset));

    }

    @Override
    public void deleteTestPreset(@PathVariable("id") Long id) {
        try {
            testPresetRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new TestingPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
        }
    }

    @Override
    public JenkinsPreset getJenkinsPresetByTestPresetName(String name) {
        try {
            TestPreset testPreset = testPresetRepository.findByName(name);
            if(testPreset != null)
                return testPreset.getJenkinsPreset();
        } catch (Exception ex) {
            throw new TestingPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
        }
        throw new TestingPresetServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
    }

    private Map<String, Object> getTestPresetServiceResponseMap(int size, Page<TestPreset> testPresetsPage) {
        Map<String, Object> response = new HashMap<>();
        if (testPresetsPage.hasContent()) {
            response.put("testPresets",  testPresetsPage.getContent().stream()
                    .map(testPreset -> presetMapperService.mapTestPresetToTestPresetDTO(testPreset))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            response.put("currentPage",  testPresetsPage.getNumber());
            response.put("limit",  size);
            response.put("totalRecords",  testPresetsPage.getTotalElements());
            response.put("totalPages",  testPresetsPage.getTotalPages());

        } else { //populate empty map
            response.put("testPresets",  new HashSet<>());
            response.put("currentPage",  0);
            response.put("limit",  size);
            response.put("totalRecords",  0l);
            response.put("totalPages",  0);
        }
        return response;
    }
}
