package com.nisum.github.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class GithubTagLinesDetails {
	
	private String tagName;
	private String filePath;
	private Set<Integer> lineNumbers;

}
