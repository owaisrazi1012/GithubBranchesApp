package com.nisum.github.domain;

import lombok.Data;

@Data
public class GithubMasterFilesDetails {
	private String name;
	private String path;
	private String sha;
	private String url;
	private String download_url;

	/*public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	public void setName(String name) {
		this.name = name;
	}
	*/

}
