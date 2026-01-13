package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа на запрос аутентификации.
 * <p>
 * Содержит JWT-токен, который возвращается клиенту после успешного входа в систему.
 * Используется в {@link com.example.bankcards.controller.AuthController} в методе
 * {@code login}, чтобы передать сгенерированный токен пользователю.
 * <p>
 * После получения токена клиент должен включать его в заголовок Authorization
 * всех последующих запросов к защищённым эндпоинтам в формате: "Bearer {token}".
 */
@Data
@AllArgsConstructor
@Schema(description = "Ответ после успешной аутентификации")
public class JwtResponse {

    /**
     * JWT-токен в формате строки.
     * Содержит закодированную информацию о пользователе (логин, роли, срок действия и др.).
     * Передаётся клиенту после успешного входа.
     * <p>
     * Пример значения: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     */
    @Schema(description = "JWT-токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}