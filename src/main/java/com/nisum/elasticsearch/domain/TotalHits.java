package com.nisum.elasticsearch.domain;

import lombok.Data;

@Data
public class TotalHits {
	private Integer value;
	private String relation;
}
