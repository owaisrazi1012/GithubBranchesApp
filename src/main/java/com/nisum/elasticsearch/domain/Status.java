package com.nisum.elasticsearch.domain;

public enum Status {

    Passed("passed"),
    Failed("failed"),
    Skipped("skipped");
    public final String value;

    Status(String value) {
        this.value = value;
    }

    public static String getName(String value) {
        for (Status status : values()) {
            if (status.value.equals(value)) {
                return status.name();
            }
        }
        return null;
    }
}
