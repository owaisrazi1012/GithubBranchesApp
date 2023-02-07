package com.nisum.api.preset.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.nisum.api.preset.domain.dto.MavenConfigParamRequestDTO;
import com.nisum.api.preset.domain.dto.MavenConfigParamResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@ToString(exclude = { "mavenConfigParams" })
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "execution_preset")
public class ExecutionPreset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique=true)
	private String name;

	@JsonManagedReference
	@OneToMany(mappedBy = "executionPreset", fetch = FetchType.LAZY,
			cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<MavenConfigParam> mavenConfigParams = new HashSet<>();

	@JsonManagedReference
	@OneToMany(mappedBy = "executionPreset", fetch = FetchType.LAZY,
			cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<TestPreset> testPresets = new HashSet<>();

	public Set<MavenConfigParamResponseDTO> getMavenConfigParamsResponse() {
		return this.getMavenConfigParams().stream().map(x->new MavenConfigParamResponseDTO(x.getId(), x.getMavenKey(), x.getMavenValue())).collect(Collectors.toSet());
	}

	public void setMavenConfigParams(Set<MavenConfigParamRequestDTO> mavenConfigParams) {
		this.mavenConfigParams = mavenConfigParams.stream().map(x->new MavenConfigParam(x.getKey(), x.getValue())).collect(Collectors.toSet());
	}

	public void addMavenConfigParam(MavenConfigParam mavenConfigParam){
		this.getMavenConfigParams().add(mavenConfigParam);
		mavenConfigParam.setExecutionPreset(this);
	}
	public void removeMavenConfigParam(MavenConfigParam mavenConfigParam){
		mavenConfigParams.remove(mavenConfigParam);
	}

	public void addTestPreset(TestPreset testPreset){
		testPresets.add(testPreset);
		testPreset.setExecutionPreset(this);
	}
	public void removeMavenConfigParam(TestPreset testPreset){
		testPresets.remove(testPreset);
		testPreset.setExecutionPreset(null);
	}
}
