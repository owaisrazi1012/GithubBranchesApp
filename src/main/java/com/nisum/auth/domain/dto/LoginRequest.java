package com.nisum.auth.domain.dto;

import javax.validation.constraints.Email;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
	@NotBlank
	@Email(message="Please provide a valid email address",regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}")
	private String emailId;

	@NotBlank
	private String password;
}
