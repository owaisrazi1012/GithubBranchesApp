package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import static com.nisum.util.GenericUtils.formatPercentage;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectScenariosCounts {
    private String projectName;
    private String scenarioName;
    private String featureName;
    private String stepName;
    private int buildNumber;
    private String buildDate;
    private int failedScenariosCount;
    private int passedScenariosCount;
    private int skippedScenariosCount;
    private int totalScenariosCount;
    private float failedScenariosPercentage;
    private float passedScenariosPercentage;
    private float skippedScenariosPercentage;

    public ProjectScenariosCounts() {
        this.failedScenariosCount = 0;
        this.passedScenariosCount = 0;
        this.skippedScenariosCount = 0;
        this.failedScenariosPercentage = 0;
        this.passedScenariosPercentage = 0;
        this.skippedScenariosPercentage = 0;
        this.totalScenariosCount = 0;
    }

    public ProjectScenariosCounts mapValues(String project, int buildNumber, String buildDate, String scenarioName, int failed, int passed, int skipped) {
        this.projectName = project;
        this.buildNumber = buildNumber;
        this.buildDate = buildDate;
        this.scenarioName = scenarioName;
        this.failedScenariosCount = failed;
        this.passedScenariosCount = passed;
        this.skippedScenariosCount = skipped;
        this.totalScenariosCount = failed + passed + skipped;
        this.failedScenariosPercentage = 100 * formatPercentage((float) this.failedScenariosCount / this.totalScenariosCount);
        this.skippedScenariosPercentage = 100 * formatPercentage((float) this.skippedScenariosPercentage / this.totalScenariosCount);
        this.passedScenariosPercentage = 100 * formatPercentage((float) this.passedScenariosCount / this.totalScenariosCount);

        return this;
    }
}