package com.nisum.api.preset.service;

import com.nisum.api.preset.domain.dto.JiraPresetRequestDTO;
import com.nisum.api.preset.domain.dto.JiraPresetResponseDTO;
import com.nisum.exception.custom.JiraPresetServiceException;
import java.util.Map;

public interface JiraPresetService {
    Map<String, Object> getAllJiraPresets(int page, int size) throws JiraPresetServiceException;

    JiraPresetResponseDTO getJiraPresetDetails(Long id) throws JiraPresetServiceException;

    JiraPresetResponseDTO createJiraPreset(JiraPresetRequestDTO jiraPresetRequestDTO) throws JiraPresetServiceException;

    JiraPresetResponseDTO updateJiraPreset(Long id, JiraPresetRequestDTO jiraPresetRequestDTO) throws JiraPresetServiceException;

    void deleteJiraPreset(Long id) throws JiraPresetServiceException;
}
