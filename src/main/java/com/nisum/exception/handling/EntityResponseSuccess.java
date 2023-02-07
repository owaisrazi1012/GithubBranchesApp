package com.nisum.exception.handling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class EntityResponseSuccess<T>  implements EntityResponse{

    private static final String MESSAGE = "Request Executed Successfully";
    ResponseEntity<Map<String, Object>> response;

    public EntityResponseSuccess(T data){
        response = new ResponseEntity<>(getDataMap(data, MESSAGE), HttpStatus.OK);
    }

    public EntityResponseSuccess(T data, String customMessage){
        response = new ResponseEntity<>(getDataMap(data, customMessage), HttpStatus.OK);
    }

    public EntityResponseSuccess(T data, HttpStatus httpStatus){
        response = new ResponseEntity<>(getDataMap(data, MESSAGE), httpStatus);
    }

    public EntityResponseSuccess(T data, String customMessage, HttpStatus httpStatus){
        response = new ResponseEntity<>(getDataMap(data, customMessage), httpStatus);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getResponse(){
        return this.response;
    }

    private Map<String, Object> getDataMap(T data, String message){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("data", data);
        dataMap.put("isSuccessful", Boolean.TRUE);
        dataMap.put("message", message);
        return dataMap;
    }
}
