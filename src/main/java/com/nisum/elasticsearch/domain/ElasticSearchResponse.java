package com.nisum.elasticsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticSearchResponse {

    public ArrayList<FeatureData> data;
    private int failedFeatureCount;
    private int passedFeatureCount;
    public String triggerDate;
    public String project;
}
