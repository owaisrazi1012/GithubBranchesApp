package com.nisum.exception.custom;

import com.nisum.exception.handling.ResponseModel;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.function.Supplier;

@Value
@EqualsAndHashCode(callSuper = false)
public class TestingPresetServiceException extends RuntimeException implements Supplier<TestingPresetServiceException> {

    private final int status;
    private final ResponseModel responseModel;

    public TestingPresetServiceException(int status, ResponseModel responseModel, Throwable cause) {
        super(cause);
        this.responseModel = responseModel;
        this.status = status;
    }
    public TestingPresetServiceException(int status, String message, Throwable cause) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), cause);
    }

    @Override
    public TestingPresetServiceException get() {
        return this;
    }
}
