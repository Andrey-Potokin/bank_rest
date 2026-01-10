package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.UserRole;

public interface UserService {

    UserResponse createUser(UserCreateRequest request, String password);

    UserResponse getUserById(Long id);

    void updateRole(Long userId, UserRole role);

    void deleteUser(Long userId);

}