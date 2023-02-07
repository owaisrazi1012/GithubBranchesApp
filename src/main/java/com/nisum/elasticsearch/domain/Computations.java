package com.nisum.elasticsearch.domain;

import lombok.Data;

@Data
public class Computations {
    PassFailPercentage featuresSummary;
    PassFailPercentage scenariosSummary;
    PassFailPercentage stepsSummary;

    public Computations() {
        this.featuresSummary = new PassFailPercentage();
        this.scenariosSummary = new PassFailPercentage();
        this.stepsSummary = new PassFailPercentage();
    }
}