package com.nisum.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class JwtAuthenticationResponse {
	private String accessToken;
	private String userName;
	private String userEmail;
	private String tokenType;

}
