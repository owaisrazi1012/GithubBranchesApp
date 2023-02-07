package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ElasticEmbeddings {

	private String data;

	@JsonProperty("mime_type")
	private String mimeType;
}
