package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.List;

@Data
public class ElasticBeforeAfter {
	private Result result;
	private List<ElasticEmbeddings> embeddings;
}
