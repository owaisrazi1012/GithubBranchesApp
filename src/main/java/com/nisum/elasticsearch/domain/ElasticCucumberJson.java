package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ElasticCucumberJson {
	private List<FeatureData> data;
	private int failedFeatureCount;
	private int passedFeatureCount;
	private String project;
	private int buildNumber;
	private String triggerDate;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static Map<String, Object> getAsMap(final ElasticCucumberJson response) {
		return OBJECT_MAPPER.convertValue(response, new TypeReference<Map<String, Object>>() {
		});
	}
}
