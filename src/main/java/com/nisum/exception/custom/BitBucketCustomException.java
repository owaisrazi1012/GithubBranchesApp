package com.nisum.exception.custom;

import com.nisum.exception.handling.ResponseModel;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.function.Supplier;

@Value
@EqualsAndHashCode(callSuper = false)
public class BitBucketCustomException extends RuntimeException implements Supplier<BitBucketCustomException> {

    private final int status;
    private final ResponseModel responseModel;

    public BitBucketCustomException(int status, ResponseModel responseModel, Throwable cause) {
        super(cause);
        this.responseModel = responseModel;
        this.status = status;
    }
    public BitBucketCustomException(int status, String message, Throwable cause) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), cause);
    }

    public BitBucketCustomException(int status, String message) {
        this(status, ResponseModel.builder().statusCode(status).message(message).build(), new Throwable());
    }

    @Override
    public BitBucketCustomException get() {
        return this;
    }
}
