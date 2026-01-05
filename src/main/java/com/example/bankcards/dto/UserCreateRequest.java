package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO для создания пользователя системы")
public class UserCreateRequest {

    @Schema(description = "Логин пользователя (уникальный)", example = "ivan.ivanov")
    @NotBlank(message = "Поле username не может быть пустым")
    private String username;

}