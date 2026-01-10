package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest dto = UserCreateRequest.builder()
                .username("newuser")
                .build();
        String password = "password123";

        when(passwordEncoder.encode(password)).thenReturn("encoded_password");
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(100L);
            return user;
        });

        UserResponse result = userService.createUser(dto, password);

        assertEquals(100L, result.getId());
        assertEquals("newuser", result.getUsername());

        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(UserRole.USER));
    }

    @Test
    void testCreateUser_UsernameExists() {
        UserCreateRequest dto = UserCreateRequest.builder().username("existing").build();

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(dto, "password"));
    }

    @Test
    void testGetUserById_Success() {
        User user = User.builder()
                .id(200L)
                .username("testuser")
                .roles(Set.of(UserRole.ADMIN))
                .build();

        when(userRepository.findById(200L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(200L);

        assertEquals(200L, result.getId());
        assertEquals("testuser", result.getUsername());

        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(UserRole.ADMIN));
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(300L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(300L));
    }

    @Test
    void testUpdateRole_Success() {
        User user = User.builder()
                .id(400L)
                .roles(new HashSet<>())
                .build();

        when(userRepository.findById(400L)).thenReturn(Optional.of(user));

        userService.updateRole(400L, UserRole.ADMIN);

        verify(userRepository).save(user);

        assertNotNull(user.getRoles());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(UserRole.ADMIN));
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(600L)).thenReturn(true);

        userService.deleteUser(600L);

        verify(userRepository).deleteById(600L);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(700L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(700L));
    }

}