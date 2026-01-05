package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;

public interface UserService {

    UserResponse createUser(UserCreateRequest request, String password);

    UserResponse getUserById(Long id);

    void updateRole(Long userId, String role);

    void deleteUser(Long userId);

}