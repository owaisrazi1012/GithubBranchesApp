package com.nisum.api.preset.service;

import com.nisum.api.preset.domain.dto.TestPresetRequestDTO;
import com.nisum.api.preset.domain.dto.TestPresetResponseDTO;
import com.nisum.api.preset.domain.entity.JenkinsPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

public interface TestingPresetService {
    Map<String, Object> getAllTestPresets(int page, int size);

    Page<TestPreset> getAllTestPresetsPage(int page, int size);

    List<String> getAllTestPresetsName();

    TestPresetResponseDTO getTestPresetDetails(Long id);

    TestPresetResponseDTO createTestPreset(TestPresetRequestDTO testPresetRequest);

    TestPresetResponseDTO updateTestPreset(Long id, TestPresetRequestDTO testPresetRequest);

    void deleteTestPreset(@PathVariable("id") Long id);

    JenkinsPreset getJenkinsPresetByTestPresetName(String name);
}
