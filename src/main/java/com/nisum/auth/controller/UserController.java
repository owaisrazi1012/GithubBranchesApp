package com.nisum.auth.controller;

import com.nisum.auth.domain.dto.ApiResponse;
import com.nisum.auth.domain.entity.Role;
import com.nisum.auth.domain.entity.User;
import com.nisum.auth.security.CurrentUser;
import com.nisum.auth.security.UserPrincipal;
import com.nisum.auth.service.UserService;
import java.util.Map;

import com.nisum.exception.custom.ResourceNotFoundException;
import com.nisum.exception.handling.EntityResponseFailure;
import com.nisum.exception.handling.EntityResponseSuccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.util.List;


@CrossOrigin("*")
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> addUser(@Valid @RequestBody User user, @CurrentUser UserPrincipal currentUser) {
        log.info("Received user add request ");
        try {
            User newUser = userService.addUser(user, currentUser);
            return new EntityResponseSuccess<>(newUser, HttpStatus.CREATED).getResponse();
        } catch (DataIntegrityViolationException e) {
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/getUserInfo/{username}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable(value = "username") String username) {
        log.info("Received get user Info request with name {} " , username);
        try {
            User newUser = userService.getUserByName(username);
            return new EntityResponseSuccess<>(newUser, HttpStatus.OK).getResponse();
        } catch (ResourceNotFoundException e) {
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        }

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/getUserRoles")
    public ResponseEntity<Map<String, Object>> getRoles() {
        List<Role> roles = userService.getRoles();
        return new EntityResponseSuccess<>(roles, HttpStatus.OK).getResponse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIEWER') ")
    @GetMapping("/getUsers")
    public  ResponseEntity<Map<String, Object>> getUsers(@CurrentUser UserPrincipal currentUser,@RequestParam int page, @RequestParam int size) {
        log.info("Received get all user Info request");
        Map<String, Object> users = userService.getUsers(currentUser,page,size);
        return new EntityResponseSuccess<>(users, HttpStatus.OK).getResponse();
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@Valid @RequestBody User newUser,
                                                          @PathVariable(value = "userId") Long id, @CurrentUser UserPrincipal currentUser) {
        try {
            log.info("Received update user info request with id {} " , id);
            User updatedUSer = userService.updateUser(newUser, id, currentUser);
            return new EntityResponseSuccess<>(updatedUSer, HttpStatus.CREATED).getResponse();
        } catch (DataIntegrityViolationException | ResourceNotFoundException e) {
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        }

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable(value = "userId") Long id,
                                                          @CurrentUser UserPrincipal currentUser) {
        try {
            log.info("Received delete user info request with id {} " , id);
            ApiResponse apiResponse = userService.deleteUser(id, currentUser);
            return new EntityResponseSuccess<>(apiResponse, HttpStatus.OK).getResponse();
        } catch (Exception e) {
            return new EntityResponseFailure(e.getMessage(), HttpStatus.BAD_REQUEST).getResponse();
        }

    }

}
