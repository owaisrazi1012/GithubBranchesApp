package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestPresetResponseDTO {
    private Long testPresetId;
    private String name;
    private String tags;
    private Integer parallelBuilds;
    private GitPresetResponseDTO gitPreset;
    private JenkinsPresetResponseDTO jenkinsPreset;
    private ExecutionPresetResponseDTO executionPreset;
    private NotificationPresetResponseDTO notificationPreset;
    private JiraPresetResponseDTO jiraPreset;

}
