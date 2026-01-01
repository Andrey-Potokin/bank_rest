package com.example.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роль пользователя в системе")
public enum UserRole {
    @Schema(description = "Обычный пользователь")
    USER,
    @Schema(description = "Администратор")
    ADMIN
}