package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FailedScenarioWithSteps {
    private String scenario;
    private List<FailedStep> steps;

    @Override
    public String toString() {
        StringBuilder steps = new StringBuilder();
        int i = 1;
        for (FailedStep failedStep : this.steps) {
            steps.append("\tStep ").append(i++).append(": ").append(failedStep.toString());
        }
        return "Scenario :" + scenario + "\n" + steps;
    }
}
