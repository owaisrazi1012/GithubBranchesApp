package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Element {
	private String start_timestamp;
	private String scenarioTimestamp;
	private String name;
	private String scenarioName;
	private List<ElasticBeforeAfter> before;
	private List<ElasticBeforeAfter> after;
	private List<Step> steps;
	private int failedStepsCount;
	private int passedStepsCount;
	private int skippedStepsCount;
	private List<Tags> tags;
}
