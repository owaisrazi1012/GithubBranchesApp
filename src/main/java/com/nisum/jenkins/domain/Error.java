package com.nisum.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error implements Serializable {
    private String context;
    private String message;
    private String exceptionName;
}
