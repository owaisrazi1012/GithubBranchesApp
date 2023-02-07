package com.nisum.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JenkinsBuildInfo implements Serializable {
  private long estimatedDuration;
  private long duration;
  private boolean building;
  private String fullDisplayName;
  private String url;
  private String result;
  private int number;
  private List<Action> actions;
}