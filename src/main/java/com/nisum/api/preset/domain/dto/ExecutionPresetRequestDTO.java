package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionPresetRequestDTO {
    private String name;
    private Set<MavenConfigParamRequestDTO> mavenConfigParams;
    private Long testPresetId;
}

