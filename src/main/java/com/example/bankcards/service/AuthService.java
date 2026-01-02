package com.example.bankcards.service;

import com.example.bankcards.entity.UserRole;

public interface AuthService {

    String login(String username, String password);

    public void register(String username, String password, UserRole role);

}