package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Данные для аутентификации пользователя")
public class AuthRequest {
    @Schema(description = "Логин пользователя", example = "ivan.ivanov")
    private String username;

    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;
}