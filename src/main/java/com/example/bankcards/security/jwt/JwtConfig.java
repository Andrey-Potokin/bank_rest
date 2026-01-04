package com.example.bankcards.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.JwtValidationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationTime;

    @Value("${app.jwt.issuer}")
    private String issuer;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret должен быть не менее 32 символов");
        }
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuer(issuer)
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withClaim("roles",
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(java.util.stream.Collectors.toList()))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new JwtValidationException("Токен не может быть пустым");
        }

        try {
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new JwtValidationException("Невалидный JWT: " + e.getMessage());
        }
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        return decodedJWT.getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);

        if (decodedJWT.getClaim("roles").isNull()) {
            return Collections.emptyList();
        }

        return decodedJWT.getClaim("roles").asList(String.class);
    }

}