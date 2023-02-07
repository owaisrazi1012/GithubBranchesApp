package com.nisum.gitlab.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AllCommitDetails {

	private String id;

	@JsonProperty("parent_ids")
	private List<String> parentsIds;
}
