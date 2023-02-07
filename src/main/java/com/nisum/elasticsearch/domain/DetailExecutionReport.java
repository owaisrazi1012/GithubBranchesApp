package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.List;

@Data
public class DetailExecutionReport {
    List<Feature> features;
    Computations computations;

    private float failedFeaturesCount;
    private float passedFeaturesCount;
    private float skippedFeaturesCount;

    public float getTotalFeaturesCount(){
        return failedFeaturesCount + passedFeaturesCount + skippedFeaturesCount;
    }
}