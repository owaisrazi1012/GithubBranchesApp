package com.nisum.auth.service;

import com.nisum.auth.domain.dto.*;

public interface AuthenticationService {

    JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest);

    JwtAuthenticationResponse authenticateClientUser(ClientLoginRequest clientLoginRequest);

    ApiResponse verifyEmail(ForgotRequest forgotRequest);

    ApiResponse forgotPassword(ForgotRequest forgotRequest);

}
