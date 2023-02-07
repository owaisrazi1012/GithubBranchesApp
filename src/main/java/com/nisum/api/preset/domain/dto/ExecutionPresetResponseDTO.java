package com.nisum.api.preset.domain.dto;

import com.nisum.api.preset.domain.entity.ExecutionPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionPresetResponseDTO {
    private Long executionPresetId;
    private String name;

    private Set<MavenConfigParamResponseDTO> mavenConfigParams;

    private Long testPresetId;
    private Set<TestPresetResponseDTO> testPresets;

    public Set<TestPresetResponseDTO> getTestPresetsSet(ExecutionPreset executionPreset){
        if(executionPreset.getTestPresets() != null && !executionPreset.getTestPresets().isEmpty()) {
            return executionPreset.getTestPresets().stream().map(TestPreset::mapTestPresetResponseToDTO).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    public ExecutionPresetResponseDTO(ExecutionPreset executionPreset) {
        this.executionPresetId = executionPreset.getId();
        this.name = executionPreset.getName();
        this.mavenConfigParams = executionPreset.getMavenConfigParamsResponse();
        this.testPresets = this.getTestPresetsSet(executionPreset);
    }


}
