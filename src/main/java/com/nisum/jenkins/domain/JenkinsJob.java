package com.nisum.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JenkinsJob implements Serializable {
  private String jobName;
  private String url;
}
