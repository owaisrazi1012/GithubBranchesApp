package com.nisum.github.domain;



import lombok.Data;

@Data
public class GithubBranchDetails{
    public String name;
    private GithubAllBranchCommit commit;

	/*
	 * public String getName() { return name; }
	 * 
	 * public void setName(String name) { this.name = name; }
	 */
}