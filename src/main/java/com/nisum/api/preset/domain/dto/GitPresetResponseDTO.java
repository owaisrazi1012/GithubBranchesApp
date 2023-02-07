package com.nisum.api.preset.domain.dto;

import com.nisum.api.preset.domain.entity.GitPreset;
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
public class GitPresetResponseDTO {
	private Long gitPresetId;
	private String name;
	private String repoUrl;
	private String userName;
	private String accessToken;
	private String branch;

	private Long testPresetId;

	private Set<TestPresetResponseDTO> testPresets;

	public Set<TestPresetResponseDTO> getTestPresetsSet(GitPreset gitPreset){
		if(gitPreset.getTestPresets() != null && !gitPreset.getTestPresets().isEmpty()) {
			return gitPreset.getTestPresets().stream().map(testPreset -> testPreset.mapTestPresetResponseToDTO()).collect(Collectors.toSet());
		}
		return new HashSet<>();
	}
	public GitPresetResponseDTO(GitPreset gitPreset) {
		this.gitPresetId = gitPreset.getId();
		this.name = gitPreset.getName();
		this.repoUrl = gitPreset.getRepoUrl();
		this.userName = gitPreset.getUserName();
		this.accessToken = gitPreset.getAccessToken();
		this.branch = gitPreset.getBranch();
		this.testPresets = this.getTestPresetsSet(gitPreset);
	}
}
