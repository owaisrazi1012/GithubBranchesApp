package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.List;

@Data
public class Match {
	private List<Argument> arguments;
}
