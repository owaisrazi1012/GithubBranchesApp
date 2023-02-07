package com.nisum.exception.custom;

import com.nisum.auth.domain.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UserAccessDeniedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private ApiResponse apiResponse;

	private String message;

	public UserAccessDeniedException(ApiResponse apiResponse) {
		super();
		this.apiResponse = apiResponse;
	}

	public UserAccessDeniedException(String message) {
		super(message);
		this.message = message;
	}

	public UserAccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiResponse getApiResponse() {
		return apiResponse;
	}

	public void setApiResponse(ApiResponse apiResponse) {
		this.apiResponse = apiResponse;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
