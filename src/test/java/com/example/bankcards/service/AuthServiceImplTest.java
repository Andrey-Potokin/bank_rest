package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void login_success() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawPassword", "encodedPassword"))
                .thenReturn(true);
        when(jwtConfig.generateToken(testUser))
                .thenReturn("jwtToken");

        String token = authService.login("testuser", "rawPassword");

        assertEquals("jwtToken", token);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("rawPassword", "encodedPassword");
        verify(jwtConfig).generateToken(testUser);
    }

    @Test
    void login_userNotFound() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            authService.login("unknown", "password");
        });

        verify(userRepository).findByUsername("unknown");
        verifyNoInteractions(passwordEncoder, jwtConfig);
    }

    @Test
    void login_invalidPassword() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            authService.login("testuser", "wrongPassword");
        });

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verifyNoInteractions(jwtConfig);
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        authService.register("newuser", "password", UserRole.USER);

        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("newuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(UserRole.USER, savedUser.getRole());
    }

    @Test
    void register_usernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.register("existing", "password", UserRole.USER);
        });

        verify(userRepository).existsByUsername("existing");
        verifyNoMoreInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

}