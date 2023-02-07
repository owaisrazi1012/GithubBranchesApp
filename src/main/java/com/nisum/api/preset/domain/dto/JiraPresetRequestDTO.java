package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraPresetRequestDTO {

    private String userName;
    private String projectId;
    private String token;
	private Long testPresetId;
}
