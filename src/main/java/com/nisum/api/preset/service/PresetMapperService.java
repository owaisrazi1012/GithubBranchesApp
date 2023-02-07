package com.nisum.api.preset.service;

import com.nisum.api.preset.domain.dto.ExecutionPresetResponseDTO;
import com.nisum.api.preset.domain.dto.GitPresetResponseDTO;
import com.nisum.api.preset.domain.dto.JenkinsPresetResponseDTO;
import com.nisum.api.preset.domain.dto.JiraPresetResponseDTO;
import com.nisum.api.preset.domain.dto.NotificationPresetResponseDTO;
import com.nisum.api.preset.domain.dto.TestPresetResponseDTO;
import com.nisum.api.preset.domain.entity.ExecutionPreset;
import com.nisum.api.preset.domain.entity.GitPreset;
import com.nisum.api.preset.domain.entity.JenkinsPreset;
import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.api.preset.domain.entity.NotificationPreset;
import com.nisum.api.preset.domain.entity.TestPreset;


public interface PresetMapperService {
    TestPresetResponseDTO mapTestPresetToTestPresetDTO(TestPreset testPreset);

    TestPresetResponseDTO mapTestPresetToDTO(TestPreset testPreset);

    ExecutionPresetResponseDTO mapExecutionPresetToDTO(ExecutionPreset executionPreset);

    JenkinsPresetResponseDTO mapJenkinsPresetToDTO(JenkinsPreset jenkinsPreset);

    GitPresetResponseDTO mapGitPresetToDTO(GitPreset gitPreset);

    NotificationPresetResponseDTO mapNotificationPresetToDTO(NotificationPreset notificationPreset);

    JiraPresetResponseDTO mapJiraPresetToDTO(JiraPreset jiraPreset);
}