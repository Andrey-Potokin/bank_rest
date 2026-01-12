package com.example.bankcards.service;

import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .roles(Set.of(UserRole.USER))
                .build();
    }

    @Test
    void login_success() {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken("testuser", "rawPassword");

        Authentication authResult = new UsernamePasswordAuthenticationToken(
                testUser,
                "rawPassword",
                testUser.getAuthorities()
        );

        when(authenticationManager.authenticate(authToken))
                .thenReturn(authResult);

        when(jwtConfig.generateToken(testUser))
                .thenReturn("jwtToken");

        String token = authService.login("testuser", "rawPassword");

        assertEquals("jwtToken", token);
        verify(authenticationManager).authenticate(authToken);
        verify(jwtConfig).generateToken(testUser);
    }

    @Test
    void login_userNotFound() {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken("unknown", "password");

        when(authenticationManager.authenticate(authToken))
                .thenThrow(new BadCredentialsException("User not found"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login("unknown", "password");
        });

        verify(authenticationManager).authenticate(authToken);
        verifyNoInteractions(jwtConfig);
    }

    @Test
    void login_invalidPassword() {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken("testuser", "wrongPassword");

        when(authenticationManager.authenticate(authToken))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login("testuser", "wrongPassword");
        });

        verify(authenticationManager).authenticate(authToken);
        verifyNoInteractions(jwtConfig);
    }
}