package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.List;

@Data
public class ElasticCountResults {
	List<Feature> features;
	private int failedFeaturesCount;
	private int passedFeaturesCount;
	private int skippedFeaturesCount;
}
