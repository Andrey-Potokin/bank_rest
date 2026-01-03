package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Данные для регистрации нового пользователя")
public class RegisterRequest {

    @Schema(description = "Логин пользователя (уникальный)", example = "ivan.ivanov")
    @NotBlank(message = "Логин не может быть пустым или состоять только из пробелов")
    @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Логин может содержать только латинские буквы, цифры, точки, подчёркивания и дефисы")
    private String username;

    @Schema(description = "Пароль пользователя", example = "password123")
    @NotBlank(message = "Пароль не может быть пустым или состоять только из пробелов")
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 10 prepared символов")
    private String password;

    @Schema(description = "Роль пользователя", example = "USER")
    @NotNull(message = "Роль пользователя обязательна")
    private UserRole role;

}