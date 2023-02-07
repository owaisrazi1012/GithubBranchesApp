package com.nisum.auth.service.impl;

import static com.nisum.util.Constants.INVALID_EMAIL_OR_PASSWORD;
import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static com.nisum.util.Constants.NO_RECORD_FOUND_OR_USER_IS_NOT_ACTIVE;
import static com.nisum.util.Constants.INPUT_FIELDS_EMPTY_MSG;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.nisum.auth.domain.dto.*;
import com.nisum.auth.domain.entity.User;
import com.nisum.auth.exception.InvalidUserException;
import com.nisum.auth.security.JwtTokenProvider;
import com.nisum.auth.service.AuthenticationService;
import com.nisum.auth.service.CustomUserDetailsService;
import com.nisum.exception.custom.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) throws InvalidUserException {

        if(loginRequest.getEmailId() != null) {
            log.info("Calling token service with email Id {} ", loginRequest.getEmailId());

            User user = userService.getUserByEmail(loginRequest.getEmailId());
            if (user.getEmail() != null && user.getUsername() != null && Boolean.TRUE.equals(user.getIsActive())) {

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                if (encoder.matches(loginRequest.getPassword(), user.getPassword()))
                {

                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(loginRequest.getEmailId(),
                                    loginRequest.getPassword()));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String jwt = jwtTokenProvider.generateToken(authentication);

                    return JwtAuthenticationResponse.builder().
                            accessToken(jwt).tokenType("Bearer")
                            .userName(authentication.getName())
                            .userEmail(loginRequest.getEmailId()).build();

                }

            else throw new InvalidUserException(UNAUTHORIZED.value(),INVALID_EMAIL_OR_PASSWORD,new Throwable());
            }

            else throw new InvalidUserException(UNAUTHORIZED.value(),NO_RECORD_FOUND_OR_USER_IS_NOT_ACTIVE,new Throwable());
        }
        else throw new InvalidUserException(UNAUTHORIZED.value(),NO_RECORD_FOUND,new Throwable());
    }

    @Override
    public JwtAuthenticationResponse authenticateClientUser(ClientLoginRequest clientLoginRequest) throws InvalidUserException {

        if (null != clientLoginRequest.getEmailId() ) {
            log.info("Calling client token service with email Id {} ", clientLoginRequest.getEmailId());

            User user =userService.getUserByEmail(clientLoginRequest.getEmailId());

         if (user.getEmail() != null && user.getUsername() != null && Boolean.TRUE.equals(user.getIsActive())) {
              UserDetails userDetails = customUserDetailsService.loadUserByUsername(clientLoginRequest.getEmailId());
               Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
               SecurityContextHolder.getContext().setAuthentication(authentication);
               String jwt = jwtTokenProvider.generateToken(authentication);

                    return JwtAuthenticationResponse.builder()
                            .accessToken(jwt).tokenType("Bearer")
                            .userName(user.getUsername())
                            .userEmail(clientLoginRequest.getEmailId()).build();
            } else throw new InvalidUserException(UNAUTHORIZED.value(),NO_RECORD_FOUND_OR_USER_IS_NOT_ACTIVE,new Throwable());

        } else throw new InvalidUserException(UNAUTHORIZED.value(),NO_RECORD_FOUND,new Throwable());

    }

    @Override
    public ApiResponse verifyEmail(ForgotRequest forgotRequest) {
        if (null != forgotRequest.getEmailId()) {
            log.info("Calling forgetPassword service and verifying emailId {} ", forgotRequest.getEmailId());

            User user = userService.getUserByEmail(forgotRequest.getEmailId());

            if (null != user.getEmail() && null != user.getUsername() && Boolean.TRUE.equals(user.getIsActive())) {
                userService.forgotPasswordToken(user);
                return new ApiResponse(Boolean.TRUE, "Verification Code has been sent to the user email : " + user.getEmail());

            } else throw new BadRequestException(new ApiResponse(Boolean.FALSE, NO_RECORD_FOUND_OR_USER_IS_NOT_ACTIVE));
        } else throw new BadRequestException(new ApiResponse(Boolean.FALSE, NO_RECORD_FOUND));
    }

    @Override
    public ApiResponse forgotPassword(ForgotRequest forgotRequest) {

        if (null != forgotRequest.getEmailId() && null != forgotRequest.getPassword() && null != forgotRequest.getToken()) {
            log.info("Calling change password service for email Id {} ", forgotRequest.getEmailId());

            if (userService.verifyToken(forgotRequest)) {
                User user = userService.getUserByEmail(forgotRequest.getEmailId());

                if (null != user && Boolean.TRUE.equals(user.getIsActive())) {
                    userService.changePassword(user, forgotRequest);

                    return new ApiResponse(Boolean.TRUE, "Password has been changed for email : " + user.getEmail());

                } else throw new BadRequestException(new ApiResponse(Boolean.FALSE,NO_RECORD_FOUND_OR_USER_IS_NOT_ACTIVE));

            } else throw new BadRequestException(new ApiResponse(Boolean.FALSE, "Invalid Email or Token"));

        } else throw new BadRequestException(new ApiResponse(Boolean.FALSE,INPUT_FIELDS_EMPTY_MSG));

    }

}
