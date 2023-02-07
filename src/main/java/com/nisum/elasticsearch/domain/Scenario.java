package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public
class Scenario {
    private String scenarioName;
    private List<StepDTO> steps;
    private float failedStepsCount;
    private float passedStepsCount;
    private float skippedStepsCount;

    private List<String> images = new ArrayList<>();

    public float getTotalStepsCount(){
        return failedStepsCount + passedStepsCount + skippedStepsCount;
    }
}
