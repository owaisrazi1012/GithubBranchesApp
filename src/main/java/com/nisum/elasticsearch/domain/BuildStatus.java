package com.nisum.elasticsearch.domain;

public enum BuildStatus {
    SUCCESS("Passed"),
    FAILURE("Failed"),
    ABORTED("Aborted");

    private final String value;

    BuildStatus(String value) {
        this.value = value;
    }

    public static String getValue(String key) {
        return BuildStatus.valueOf(key).value;
    }

    public String value() {
        return value;
    }
}