package com.nisum.unit.service.auth;

import com.nisum.api.preset.repository.PasswordResetTokenRepository;
import com.nisum.auth.domain.entity.User;
import com.nisum.auth.repository.RoleRepository;
import com.nisum.auth.repository.UserRepository;
import com.nisum.auth.security.UserPrincipal;
import com.nisum.auth.service.impl.UserServiceImpl;
import com.nisum.service.PostJenkinsActionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    PostJenkinsActionService postJenkinsActionService;

    @Test
    void shouldAddUser() {

        User user = new User();
        user.setUsername("Test");
        user.setPassword("Password");
        user.setEmail("Test@email.com");

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserPrincipal userPrincipal = new UserPrincipal(1L, "user", "test@test.com", "password", null);

        assertEquals(user, userService.addUser(user, userPrincipal));

    }

    @Test
    void shouldThrowDataIntegrityViolationExceptionWhenUsernameExist() {

        User user = new User();
        user.setUsername("Test");
        user.setPassword("Password");
        user.setEmail("Test@email.com");

        when(userRepository.existsByUsername(anyString())).thenReturn(Boolean.TRUE);

        UserPrincipal userPrincipal = new UserPrincipal(1L, "user", "test@test.com", "password", null);

        assertThrows(DataIntegrityViolationException.class, () -> userService.addUser(user, userPrincipal));

    }

    @Test
    void shouldThrowDataIntegrityViolationExceptionWhenEmailExist() {

        User user = new User();
        user.setUsername("Test");
        user.setPassword("Password");
        user.setEmail("Test@email.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(Boolean.TRUE);

        UserPrincipal userPrincipal = new UserPrincipal(1L, "user", "test@test.com", "password", null);

        assertThrows(DataIntegrityViolationException.class, () -> userService.addUser(user, userPrincipal));

    }

    @Test
    void shouldThrowDataIntegrityViolationExceptionWhenPasswordLengthIsLess() {

        User user = new User();
        user.setUsername("Test");
        user.setPassword("pass");
        user.setEmail("Test@email.com");

        UserPrincipal userPrincipal = new UserPrincipal(1L, "user", "test@test.com", "password", null);

        assertThrows(DataIntegrityViolationException.class, () -> userService.addUser(user, userPrincipal));

    }

}
