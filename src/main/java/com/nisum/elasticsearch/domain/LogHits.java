package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogHits {
	@JsonProperty("_index")
	private String index;

	@JsonProperty("_source")
	private ElasticCucumberJson source;
}
