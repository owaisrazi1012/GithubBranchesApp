package com.nisum.api.preset.domain.dto;

import com.nisum.api.preset.domain.entity.JenkinsPreset;
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
public class JenkinsPresetResponseDTO {
	private Long jenkinsPresetId;
	private String name;
	private String url;
	private String userName;
	private String password;
	private String credentialId;
	private String slave;
	private Long testPresetId;
	private Set<TestPresetResponseDTO> testPresets;

	public Set<TestPresetResponseDTO> getTestPresetsSet(JenkinsPreset jenkinsPreset){
		if(jenkinsPreset.getTestPresets() != null && !jenkinsPreset.getTestPresets().isEmpty()) {
			return jenkinsPreset.getTestPresets().stream().map(testPreset -> testPreset.mapTestPresetResponseToDTO()).collect(Collectors.toSet());
		}
		return new HashSet<>();
	}
	
	public JenkinsPresetResponseDTO(JenkinsPreset jenkinsPreset) {
		this.jenkinsPresetId = jenkinsPreset.getId();
		this.name = jenkinsPreset.getName();
		this.url = jenkinsPreset.getUrl();
		this.userName = jenkinsPreset.getUserName();
		this.userName = jenkinsPreset.getUserName();
		this.password = jenkinsPreset.getPassword();
		this.credentialId = jenkinsPreset.getCredentialId();
		this.slave = jenkinsPreset.getSlave();
		this.testPresets = this.getTestPresetsSet(jenkinsPreset);
	}
	
}
