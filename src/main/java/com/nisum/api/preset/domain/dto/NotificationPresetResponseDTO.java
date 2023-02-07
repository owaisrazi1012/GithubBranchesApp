package com.nisum.api.preset.domain.dto;

import com.nisum.api.preset.domain.entity.NotificationPreset;
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
public class NotificationPresetResponseDTO {
    private Long notificationPresetId;
    private String name;
    private String presetConfig;
    private String recipients;
	private Long testPresetId;
    private Set<TestPresetResponseDTO> testPresets;

    public Set<TestPresetResponseDTO> getTestPresetsSet(NotificationPreset notificationPreset){
        if(notificationPreset.getTestPresets() != null && !notificationPreset.getTestPresets().isEmpty()) {
            return notificationPreset.getTestPresets().stream().map(testPreset -> testPreset.mapTestPresetResponseToDTO()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
    public NotificationPresetResponseDTO(NotificationPreset notificationPreset) {
        this.notificationPresetId = notificationPreset.getId();
        this.name = notificationPreset.getName();
        this.presetConfig = notificationPreset.getPresetConfig();
        this.recipients = notificationPreset.getRecipients();
        this.testPresets = this.getTestPresetsSet(notificationPreset);
    }
}
