package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPresetRequestDTO {
    private String name;
    private String presetConfig;
    private String recipients;
	private Long testPresetId;
}
