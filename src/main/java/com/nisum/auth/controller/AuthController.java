package com.nisum.auth.controller;

import com.nisum.auth.domain.dto.*;
import com.nisum.auth.exception.InvalidUserException;
import com.nisum.auth.service.AuthenticationService;
import com.nisum.exception.custom.BadRequestException;
import com.nisum.exception.handling.EntityResponseFailure;
import com.nisum.exception.handling.EntityResponseSuccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private AuthenticationService authenticationService;


	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
			throws InvalidUserException {
		return new EntityResponseSuccess<>(authenticationService.authenticateUser(loginRequest), HttpStatus.OK).getResponse();
	}

	@PostMapping("/login-client")
	public ResponseEntity<Map<String, Object>> authenticateClientUser(@Valid @RequestBody ClientLoginRequest clientLoginRequest)
			throws InvalidUserException{
		return new EntityResponseSuccess<>(authenticationService.authenticateClientUser(clientLoginRequest), HttpStatus.OK).getResponse();
	}

	@PostMapping("/verify-email")
	public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody ForgotRequest forgetRequest) throws InvalidUserException {

		try {
			ApiResponse apiResponse = authenticationService.verifyEmail(forgetRequest);
			return new EntityResponseSuccess<>(apiResponse, HttpStatus.OK).getResponse();

		}
		catch(InvalidUserException ex ) {
			throw ex;
		}
		catch (BadRequestException ex ) {
			throw ex;
		}
		catch (Exception e) {
			return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
		}
	}

	@PostMapping("/change-password")
	public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotRequest forgotRequest) throws InvalidUserException {

		try {
			ApiResponse apiResponse = authenticationService.forgotPassword(forgotRequest);
			return new EntityResponseSuccess<>(apiResponse, HttpStatus.OK).getResponse();
		}
		catch(InvalidUserException ex ) {
			throw ex;
		}
		catch (BadRequestException ex ) {
			throw ex;
		}
		catch (Exception e) {
			return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
		}

	}

}
