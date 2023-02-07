package com.nisum.exception.custom;

import java.util.function.Supplier;

import com.nisum.exception.handling.ResponseModel;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class JiraPresetServiceException extends RuntimeException implements Supplier<JiraPresetServiceException> {

    private final int status;
    private final ResponseModel responseModel;

    public JiraPresetServiceException(int status, ResponseModel responseModel, Throwable cause) {
        super(cause);
        this.responseModel = responseModel;
        this.status = status;
    }
    public JiraPresetServiceException(int status, String message, Throwable cause) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), cause);
    }

    @Override
    public JiraPresetServiceException get() {
        return this;
    }

}
