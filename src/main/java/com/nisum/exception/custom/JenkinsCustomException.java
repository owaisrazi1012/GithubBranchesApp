package com.nisum.exception.custom;

import com.nisum.exception.handling.ResponseModel;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.function.Supplier;

@Value
@EqualsAndHashCode(callSuper = false)
public class JenkinsCustomException extends RuntimeException implements Supplier<JenkinsCustomException> {

    private final int status;
    private final ResponseModel responseModel;

    public JenkinsCustomException(int status, ResponseModel responseModel, Throwable cause) {
        super(cause);
        this.responseModel = responseModel;
        this.status = status;
    }
    public JenkinsCustomException(int status, String message, Throwable cause) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), cause);
    }

    @Override
    public JenkinsCustomException get() {
        return this;
    }
}
