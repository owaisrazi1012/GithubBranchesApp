package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.List;

@Data
public class ElasticHits {
	private List<LogHits> hits;
	private TotalHits total;
}
