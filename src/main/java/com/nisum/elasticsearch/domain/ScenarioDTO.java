package com.nisum.elasticsearch.domain;

import lombok.Data;

@Data
public class ScenarioDTO {
    private String scenarioName;
    private String status;
    private String timestamp;
    private int buildNumber;
    private String project;
}