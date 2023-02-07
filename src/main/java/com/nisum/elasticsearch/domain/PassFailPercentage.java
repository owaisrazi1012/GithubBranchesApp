package com.nisum.elasticsearch.domain;

import lombok.Data;

import static com.nisum.util.GenericUtils.formatPercentage;

@Data
public class PassFailPercentage extends PassFailCounts {
    private float failedPercentage;
    private float passedPercentage;
    private float skippedPercentage;

    public PassFailPercentage() {
        super();
        this.failedPercentage = 0;
        this.passedPercentage = 0;
        this.skippedPercentage = 0;
    }

    public PassFailPercentage mapValues(int failed, int passed, int skipped) {
        super.mapValues(failed, passed, skipped);
        return this.calculatePercentages();
    }

    public PassFailPercentage addValues(int failed, int passed, int skipped) {
        super.addValues(failed, passed, skipped);
        return this.calculatePercentages();
    }

    private PassFailPercentage calculatePercentages(){
        this.failedPercentage = 100 * formatPercentage((float) this.getFailedCount() / this.getTotalCount());
        this.skippedPercentage = 100 * formatPercentage((float) this.getSkippedCount() / this.getTotalCount());
        this.passedPercentage = 100 * formatPercentage((float) this.getPassedCount() / this.getTotalCount());
        return this;
    }
}
