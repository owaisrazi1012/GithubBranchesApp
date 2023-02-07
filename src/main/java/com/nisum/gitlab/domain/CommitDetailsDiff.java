package com.nisum.gitlab.domain;

import lombok.Data;

@Data
public class CommitDetailsDiff {

	private String old_path;
	private String new_path;
	private Boolean new_file;
	private Boolean renamed_file;
	private Boolean deleted_file;
}
