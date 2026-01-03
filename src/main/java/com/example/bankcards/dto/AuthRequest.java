package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Данные для аутентификации пользователя")
public class AuthRequest {

    @Schema(description = "Логин пользователя", example = "ivan.ivanov")
    @NotBlank(message = "Логин не может быть пустым или состоять только из пробелов")
    private String username;

    @Schema(description = "Пароль пользователя", example = "password123")
    @NotBlank(message = "Пароль не может быть пустым или состоять только из пробелов")
    private String password;

}