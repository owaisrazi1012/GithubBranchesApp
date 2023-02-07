package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FailedStep {
    private String step;
    private String status;

    @Override
    public String toString() {
        return step + " : " + status + "\n";
    }
}