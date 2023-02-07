package com.nisum.exception.handling;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
public class EntityResponseFailure implements EntityResponse{

    private static final String message = "Something Went Wrong";
    public ResponseEntity<Map<String, Object>> response;

    public EntityResponseFailure(){
        response = new ResponseEntity<>(getDataMap(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public EntityResponseFailure(String customMessage){
        response = new ResponseEntity<>(getDataMap(customMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public EntityResponseFailure(String customMessage, HttpStatus httpStatus){
        response = new ResponseEntity<>(getDataMap(customMessage), httpStatus);
    }

    public EntityResponseFailure(ResponseModel body, HttpHeaders headers, HttpStatus status) {
        response = new ResponseEntity<>(getDataMap(body), headers, status);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getResponse(){
        return this.response;
    }

    private Map<String, Object> getDataMap(String message){
        return getDataMap(getErrorModel(message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    private Map<String, Object> getDataMap(ResponseModel responseModel){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("data", new HashSet<>());
        dataMap.put("isSuccessful", Boolean.FALSE);
        dataMap.put("message", responseModel.getMessage());
        return dataMap;
    }

    private ResponseModel getErrorModel(String message, Integer httpStatusCode){
       return ResponseModel.builder()
                .statusCode(httpStatusCode)
                .message(message)
                .build();
    }


}
