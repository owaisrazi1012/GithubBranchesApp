package com.nisum.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureData {
    private List<Element> elements;
    private int failedScenariosCount;
    private int passedScenariosCount;
    private String name;
    private String featureName;
    private List<Tags> tags;

    public FeatureData format() {
        this.featureName = this.name;
        this.name = null;
        for(Element element: this.elements){
            element.setScenarioName(element.getName());
            element.setName(null);
            element.setScenarioTimestamp(element.getStart_timestamp());
            element.setStart_timestamp(null);
            for(Step step: element.getSteps()){
                step.setStepName(step.getName());
                step.setName(null);
            }
        }
        return this;
    }
}