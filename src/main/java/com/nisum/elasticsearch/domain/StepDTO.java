package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StepDTO extends ScenarioDTO {
    private String stepName;
    private String duration;
    private String status;
}