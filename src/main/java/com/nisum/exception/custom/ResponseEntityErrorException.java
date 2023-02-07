package com.nisum.exception.custom;

import com.nisum.auth.domain.dto.ApiResponse;
import org.springframework.http.ResponseEntity;

public class ResponseEntityErrorException extends RuntimeException {
	private static final long serialVersionUID = -3156815846745801694L;

	private final transient ResponseEntity<ApiResponse> apiResponse;

	public ResponseEntityErrorException(ResponseEntity<ApiResponse> apiResponse) {
		this.apiResponse = apiResponse;
	}

	public ResponseEntity<ApiResponse> getApiResponse() {
		return apiResponse;
	}
}
