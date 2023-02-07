package com.nisum.api.preset.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "maven_config_param")
public class MavenConfigParam {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String mavenKey;

	private String mavenValue;

	@JsonBackReference
	@ManyToOne
	private ExecutionPreset executionPreset;

	public MavenConfigParam(String key, String value, ExecutionPreset executionPreset) {
		this.mavenKey = key;
		this.mavenValue = value;
		this.executionPreset = executionPreset;
	}
	public MavenConfigParam(String key, String value) {
		this.mavenKey = key;
		this.mavenValue = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MavenConfigParam )) return false;
		return id != null && id.equals(((MavenConfigParam) o).getId());
	}
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	public MavenConfigParam getMavenConfigParam(){
		return this;
	}


}
