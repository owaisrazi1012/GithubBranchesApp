package com.nisum.api.preset.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitPresetRequestDTO {
	private String name;
	private String repoUrl;
	private String userName;
	private String accessToken;
	private String branch;
	private Long testPresetId;
}
