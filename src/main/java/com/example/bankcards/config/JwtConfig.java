package com.example.bankcards.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    public String generateToken(String username) {
        JWTCreator.Builder tokenBuilder = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000)); // 1 час

        return tokenBuilder.sign(algorithm);
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm)
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(algorithm)
                .build()
                .verify(token);
        return decodedJWT.getSubject();
    }

}