package com.nisum.exception.handling;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface EntityResponse {

    ResponseEntity<Map<String, Object>> getResponse();
}
