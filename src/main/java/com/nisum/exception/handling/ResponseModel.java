package com.nisum.exception.handling;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ResponseModel implements Serializable {
    String message;
    Integer statusCode;
}
