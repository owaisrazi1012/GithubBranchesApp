package com.nisum.elasticsearch.domain;

import lombok.Data;

@Data
public class PassFailCounts {
    private int failedCount;
    private int passedCount;
    private int skippedCount;
    private float totalCount;

    public PassFailCounts() {
        this.failedCount = 0;
        this.passedCount = 0;
        this.skippedCount = 0;
        this.totalCount = 0;
    }

    public PassFailCounts mapValues(int failed, int passed, int skipped) {
        this.failedCount = failed;
        this.passedCount = passed;
        this.skippedCount = skipped;
        this.totalCount = failed + passed + skipped;
        return this;
    }

    public PassFailCounts addValues(int failed, int passed, int skipped) {
        this.failedCount = this.failedCount + failed;
        this.passedCount = this.passedCount + passed;
        this.skippedCount = this.skippedCount + skipped;
        this.totalCount = this.failedCount + this.passedCount + this.skippedCount;
        return this;
    }
}
