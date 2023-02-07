package com.nisum.api.preset.service;

import com.nisum.api.preset.domain.dto.JenkinsPresetRequestDTO;
import com.nisum.api.preset.domain.dto.JenkinsPresetResponseDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface JenkinsPresetService {
    Map<String, Object> getAllJenkinsPresets(int page, int size);

    JenkinsPresetResponseDTO getJenkinsPresetDetails(Long id);

    JenkinsPresetResponseDTO createJenkinsPreset(@RequestBody JenkinsPresetRequestDTO jenkinsPresetRequest);

    JenkinsPresetResponseDTO updateJenkinsPreset(Long id, JenkinsPresetRequestDTO jenkinsPresetRequest);

    void deleteJenkinsPreset(Long id);
}
