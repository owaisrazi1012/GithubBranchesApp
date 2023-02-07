package com.nisum.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder
public class Parameter {
  private String clazz;
  private String name;
  private String value;
}
