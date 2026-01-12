package com.example.bankcards.service;

import com.example.bankcards.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;

    @Override
    public String login(String username, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authToken);

        return jwtConfig.generateToken(authentication.getPrincipal());
    }
}