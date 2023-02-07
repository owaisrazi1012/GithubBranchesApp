package com.nisum.api.preset.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JenkinsPresetRequestDTO {
	private String name;
	private String url;
	private String userName;
	private String password;
	private String credentialId;
	private String slave;
	private Long testPresetId;
}
