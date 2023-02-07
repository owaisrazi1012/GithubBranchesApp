package com.nisum.api.preset.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nisum.api.preset.domain.dto.TestPresetResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "test_preset")
public class TestPreset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique=true)
	private String name;

	private String tags;
	private Integer parallelBuilds;

	@JsonBackReference
	@ManyToOne
	private JenkinsPreset jenkinsPreset;

	@JsonBackReference
	@ManyToOne
	private ExecutionPreset executionPreset;

	@JsonBackReference(value="gitPreset")
	@ManyToOne
	private GitPreset gitPreset;

	@JsonBackReference(value="jiraPreset")
	@ManyToOne
	private JiraPreset jiraPreset;

	@JsonBackReference
	@ManyToOne
	private NotificationPreset notificationPreset;

	public TestPresetResponseDTO mapTestPresetResponseToDTO(){
		TestPresetResponseDTO testPresetResponseDTO = new TestPresetResponseDTO();
		testPresetResponseDTO.setTestPresetId(this.getId());
		testPresetResponseDTO.setName(this.getName());
		return testPresetResponseDTO;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TestPreset )) return false;
		return id != null && id.equals(((TestPreset) o).getId());
	}
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}