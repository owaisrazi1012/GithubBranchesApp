package com.nisum.exception.custom;

import com.nisum.exception.handling.ResponseModel;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.function.Supplier;

@Value
@EqualsAndHashCode(callSuper = false)
public class ExecutionServiceException extends RuntimeException implements Supplier<ExecutionServiceException> {

    private final int status;
    private final ResponseModel responseModel;

    public ExecutionServiceException(int status, ResponseModel responseModel, Throwable cause) {
        super(cause);
        this.responseModel = responseModel;
        this.status = status;
    }
    public ExecutionServiceException(int status, String message, Throwable cause) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), cause);
    }

    @Override
    public ExecutionServiceException get() {
        return this;
    }
}
