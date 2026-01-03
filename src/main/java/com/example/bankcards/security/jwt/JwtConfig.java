package com.example.bankcards.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.bankcards.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("JWT инициализирован. Issuer: {}, срок действия: {} мс", issuer, expirationTime);
    }


    public String generateToken(User user) {
        String token = JWT.create()
                .withSubject(user.getUsername())
                .withIssuer(issuer)
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withClaim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .sign(algorithm);

        if (log.isDebugEnabled()) {
            log.debug("[requestId={}] Токен сгенерирован для пользователя: {}",
                    MDC.get("requestId"), user.getUsername());
        }
        return token;
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            if (log.isDebugEnabled()) {
                log.debug("[requestId={}] Токен валидирован успешно", MDC.get("requestId"));
            }
            return true;
        } catch (JWTVerificationException e) {
            log.warn("[requestId={}] Неверный токен (подпись/срок/issuer). Причина: {}",
                    MDC.get("requestId"), e.getClass().getSimpleName());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("[requestId={}] Некорректный формат токена. Причина: {}",
                    MDC.get("requestId"), e.getClass().getSimpleName());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            String username = decodedJWT.getSubject();
            if (log.isDebugEnabled()) {
                log.debug("[requestId={}] Из токена извлечён username: {}",
                        MDC.get("requestId"), username);
            }
            return username;
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new RuntimeException("Недопустимый токен JWT", e);
        }
    }

}