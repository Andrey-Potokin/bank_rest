package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO для представления пользователя системы")
public class UserResponse {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @Schema(description = "Логин пользователя", example = "ivan.ivanov")
    private String username;

    @Schema(description = "Роль пользователя в системе", example = "USER")
    private UserRole role;

}