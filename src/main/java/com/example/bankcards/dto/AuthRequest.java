package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для передачи данных аутентификации пользователя.
 * <p>
 * Используется при входе в систему через {@link com.example.bankcards.controller.AuthController}.
 * Содержит логин и пароль пользователя, необходимые для генерации JWT-токена.
 *
 * <p>Поля класса проходят валидацию:
 * <ul>
 *   <li>Логин не может быть пустым или состоять только из пробелов</li>
 *   <li>Пароль не может быть пустым или состоять только из пробелов</li>
 * </ul>
 */
@Data
@Schema(description = "Данные для аутентификации пользователя")
public class AuthRequest {

    /**
     * Логин пользователя, используемый для входа в систему.
     * Должен быть уникальным, не может быть пустым или состоять только из пробелов.
     * Пример: "ivan.ivanov"
     */
    @Schema(description = "Логин пользователя", example = "ivan.ivanov")
    @NotBlank(message = "Логин не может быть пустым или состоять только из пробелов")
    private String username;

    /**
     * Пароль пользователя для аутентификации.
     * Должен соответствовать требованиям безопасности, не может быть пустым или состоять только из пробелов.
     * Передаётся в открытом виде (должен шифроваться на уровне HTTPS).
     * Пример: "password123"
     */
    @Schema(description = "Пароль пользователя", example = "password123")
    @NotBlank(message = "Пароль не может быть пустым или состоять только из пробелов")
    private String password;
}