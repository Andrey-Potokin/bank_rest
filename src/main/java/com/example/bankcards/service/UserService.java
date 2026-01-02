package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;

public interface UserService {

    UserDto createUser(UserDto userDto, String password);

    UserDto getUserById(Long id);

    void updateRole(Long userId, String role);

    void deleteUser(Long userId);

}