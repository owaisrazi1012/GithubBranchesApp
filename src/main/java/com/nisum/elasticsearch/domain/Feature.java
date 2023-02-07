package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.List;

@Data
public class Feature {
	private String featureName;
	private List<Scenario> scenarios;
	private float failedScenariosCount;
	private float passedScenariosCount;
	private float skippedScenariosCount;

	public float getTotalScenariosCount(){
		return failedScenariosCount + passedScenariosCount + skippedScenariosCount;
	}
}