package com.nisum.api.preset.service.impl;


import com.nisum.api.preset.domain.dto.ExecutionEngineDTO;
import com.nisum.api.preset.domain.dto.ExecutionPresetResponseDTO;
import com.nisum.api.preset.domain.dto.GitPresetResponseDTO;
import com.nisum.api.preset.domain.dto.JenkinsPresetResponseDTO;
import com.nisum.api.preset.domain.dto.NotificationPresetResponseDTO;
import com.nisum.api.preset.domain.dto.TestPresetResponseDTO;
import com.nisum.api.preset.repository.ExecutionPresetRepository;
import com.nisum.api.preset.repository.GitPresetRepository;
import com.nisum.api.preset.repository.JenkinsPresetRepository;
import com.nisum.api.preset.repository.NotificationPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.PresetConfigService;
import com.nisum.api.preset.service.PresetMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Profile({"dev", "qa"})
public class PresetConfigServiceImpl implements PresetConfigService {
    @Autowired
    private TestPresetRepository testPresetRepository;

    @Autowired
    private ExecutionPresetRepository executionPresetRepository;

    @Autowired
    private JenkinsPresetRepository jenkinsPresetRepository;

    @Autowired
    private NotificationPresetRepository notificationPresetRepository;

    @Autowired
    private GitPresetRepository gitPresetRepository;

    @Autowired
    PresetMapperService presetMapperService;

    @Override
    public ExecutionEngineDTO getExecutionEnginePresets() {
        List<TestPresetResponseDTO> testPresets = Streamable.of(testPresetRepository.findAll()).stream().map(testPreset -> presetMapperService.mapTestPresetToTestPresetDTO(testPreset)).collect(Collectors.toList());
        List<ExecutionPresetResponseDTO> executionPresets = Streamable.of(executionPresetRepository.findAll()).stream().map(executionPreset -> presetMapperService.mapExecutionPresetToDTO(executionPreset)).collect(Collectors.toList());
        List<JenkinsPresetResponseDTO> jenkinsPresets = Streamable.of(jenkinsPresetRepository.findAll()).stream().map(jenkinsPreset -> presetMapperService.mapJenkinsPresetToDTO(jenkinsPreset)).collect(Collectors.toList());
        List<GitPresetResponseDTO> gitPresets = Streamable.of(gitPresetRepository.findAll()).stream().map(gitPreset -> presetMapperService.mapGitPresetToDTO(gitPreset)).collect(Collectors.toList());
        List<NotificationPresetResponseDTO> notificationPresets = Streamable.of(notificationPresetRepository.findAll()).stream().map(notificationPreset -> presetMapperService.mapNotificationPresetToDTO(notificationPreset)).collect(Collectors.toList());

       return ExecutionEngineDTO.builder()
                .testPresets(testPresets)
                .executionPresets(executionPresets)
                .jenkinsPresets(jenkinsPresets)
                .gitPresets(gitPresets)
                .notificationPresets(notificationPresets).build();

    }
}