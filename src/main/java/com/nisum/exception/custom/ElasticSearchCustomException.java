package com.nisum.exception.custom;

import com.nisum.exception.handling.ResponseModel;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.function.Supplier;

@Value
@EqualsAndHashCode(callSuper = false)
public class ElasticSearchCustomException extends RuntimeException implements Supplier<ElasticSearchCustomException> {

    private final int status;
    private final ResponseModel responseModel;

    public ElasticSearchCustomException(int status, ResponseModel responseModel, Throwable cause) {
        super(cause);
        this.responseModel = responseModel;
        this.status = status;
    }
    public ElasticSearchCustomException(int status, String message, Throwable cause) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), cause);
    }

    public ElasticSearchCustomException(int status, String message) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), new Throwable());
    }

    @Override
    public ElasticSearchCustomException get() {
        return this;
    }
}
