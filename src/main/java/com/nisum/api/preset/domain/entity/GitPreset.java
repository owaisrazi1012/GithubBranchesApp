package com.nisum.api.preset.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "git_preset")
public class GitPreset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique=true)
	private String name;

	private String repoUrl;

	private String userName;

	private String accessToken;

	private String branch;

	@JsonManagedReference
	@OneToMany(mappedBy = "gitPreset", fetch = FetchType.LAZY,
			cascade = CascadeType.ALL)
	private Set<TestPreset> testPresets = new HashSet<>();



	public void addTestPreset(TestPreset testPreset){
		testPresets.add(testPreset);
		testPreset.setGitPreset(this);
	}
	public void removeMavenConfigParam(TestPreset testPreset){
		testPresets.remove(testPreset);
		testPreset.setGitPreset(null);
	}
}
