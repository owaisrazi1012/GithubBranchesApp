package com.nisum.api.preset.domain.dto;

import com.nisum.api.preset.domain.entity.GitPreset;
import com.nisum.api.preset.domain.entity.JiraPreset;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraPresetResponseDTO {
    private Long jiraPresetId;
    private String userName;
    private String projectId;
    private String token;

    private Long testPresetId;
    private Set<TestPresetResponseDTO> testPresets;
    public Set<TestPresetResponseDTO> getTestPresetsSet(JiraPreset jiraPreset){
        if(jiraPreset.getTestPresets() != null && !jiraPreset.getTestPresets().isEmpty()) {
            return jiraPreset.getTestPresets().stream().map(testPreset -> testPreset.mapTestPresetResponseToDTO()).collect(
                    Collectors.toSet());
        }
        return new HashSet<>();
    }

    public JiraPresetResponseDTO(JiraPreset jiraPreset) {
        this.jiraPresetId = jiraPreset.getId();
        this.userName = jiraPreset.getUserName();
        this.projectId = jiraPreset.getProjectId();
        this.token = jiraPreset.getToken();
        this.testPresets = this.getTestPresetsSet(jiraPreset);
    }


}
