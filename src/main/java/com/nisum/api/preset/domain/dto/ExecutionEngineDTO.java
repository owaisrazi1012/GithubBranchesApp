package com.nisum.api.preset.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutionEngineDTO {

    private List<TestPresetResponseDTO> testPresets;
    private List<JenkinsPresetResponseDTO> jenkinsPresets;
    private List<ExecutionPresetResponseDTO> executionPresets;
    private List<GitPresetResponseDTO> gitPresets;
    private List<NotificationPresetResponseDTO> notificationPresets;
}
