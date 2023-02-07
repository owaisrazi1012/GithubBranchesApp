package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestPresetRequestDTO {
    private String name;
    private String tags;
    private Integer parallelBuilds;
    private Long gitPresetId;
    private Long jenkinsPresetId;
    private Long executionPresetId;
    private Long notificationPresetId;
    private Long jiraPresetId;
}
