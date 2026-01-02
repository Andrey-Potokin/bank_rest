package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления пользователя системы")
public class UserDto {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @Schema(description = "Логин пользователя (уникальный)", example = "ivan.ivanov")
    @NotBlank(message = "Поле username не может быть пустым")
    private String username;

    @Schema(description = "Роль пользователя в системе", example = "USER")
    private UserRole role;

}