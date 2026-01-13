package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * DTO (Data Transfer Object) для создания нового пользователя в системе.
 * <p>
 * Используется при передаче данных от клиента к серверу при регистрации или
 * административном создании пользователя. Содержит минимальную информацию,
 * необходимую для создания учётной записи — в текущей реализации это логин.
 * <p>
 * Поле проходит валидацию на стороне сервера: логин не может быть пустым
 * или состоять только из пробельных символов.
 * <p>
 * Пример использования:
 * <pre>
 * {
 *   "username": "ivan.ivanov"
 * }
 * </pre>
 */
@Data
@Builder
@Schema(description = "DTO для создания пользователя системы")
public class UserCreateRequest {

    /**
     * Уникальный логин пользователя, используемый для входа в систему.
     * <p>
     * Должен быть непустым и уникальным в пределах системы.
     * Регистр может учитываться или игнорироваться в зависимости от реализации сервиса.
     * <p>
     * Пример: "ivan.ivanov"
     */
    @Schema(description = "Логин пользователя (уникальный)", example = "ivan.ivanov")
    @NotBlank(message = "Поле username не может быть пустым")
    private String username;
}