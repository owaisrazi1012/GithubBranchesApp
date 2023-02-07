package com.nisum.auth.service;

import com.nisum.auth.domain.dto.ApiResponse;
import com.nisum.auth.domain.dto.ForgotRequest;
import com.nisum.auth.domain.entity.Role;
import com.nisum.auth.domain.entity.User;
import com.nisum.auth.security.UserPrincipal;

import java.util.List;
import java.util.Map;

public interface UserService {

    User addUser(User user, UserPrincipal currentUser);

    User updateUser(User newUser, Long id, UserPrincipal currentUser);

    User changePassword(User newUser, ForgotRequest forgotRequest);

    ApiResponse deleteUser(Long id, UserPrincipal currentUser);

    User getUserByName(String username);

    User getUserByEmail(String username);

    User forgotPasswordToken(User user);

    boolean verifyToken(ForgotRequest request);

    List<Role> getRoles();

    Map<String, Object> getUsers(UserPrincipal currentUser, int page, int size);

}
