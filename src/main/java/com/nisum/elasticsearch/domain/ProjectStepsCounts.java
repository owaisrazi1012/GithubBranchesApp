package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import static com.nisum.util.GenericUtils.formatPercentage;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectStepsCounts {
    private String scenarioName;
    private String stepName;
    private String status;
    private String duration;
    private int buildNumber;
    private String triggerDate;
    private int failedStepsCount;
    private int passedStepsCount;
    private int skippedStepsCount;
    private float failedStepsPercentage;
    private float passedStepsPercentage;
    private float skippedStepsPercentage;
    private int totalStepsCount;

    public ProjectStepsCounts() {
        this.failedStepsCount = 0;
        this.passedStepsCount = 0;
        this.skippedStepsCount = 0;
        this.failedStepsPercentage = 0;
        this.passedStepsPercentage = 0;
        this.skippedStepsPercentage = 0;
        this.totalStepsCount = 0;
    }

    public ProjectStepsCounts mapValues(String scenarioName, String stepName, int failed, int passed, int skipped) {
        this.scenarioName = scenarioName;
        this.stepName = stepName;
        this.failedStepsCount = failed;
        this.passedStepsCount = passed;
        this.skippedStepsCount = skipped;
        this.totalStepsCount = failed + passed + skipped;
        this.failedStepsPercentage = 100 * formatPercentage( (float) this.failedStepsCount / this.totalStepsCount);
        this.skippedStepsPercentage = 100 * formatPercentage( (float) this.skippedStepsCount/this.totalStepsCount);
        this.passedStepsPercentage = 100 * formatPercentage( (float) this.passedStepsCount/this.totalStepsCount);
        return this;
    }
}