package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * DTO (Data Transfer Object) для передачи данных о пользователе в ответах API.
 * <p>
 * Используется контроллерами и сервисами для возврата информации о пользователе
 * клиенту в сериализуемом формате (например, в виде JSON).
 * Содержит идентификатор, логин и набор ролей пользователя.
 * <p>
 * Поле {@code roles} представляет собой множество ролей ({@link UserRole}),
 * что позволяет одному пользователю иметь несколько ролей (например, USER и ADMIN).
 */
@Data
@Builder
@Schema(description = "DTO для представления пользователя системы")
public class UserResponse {

    /**
     * Уникальный идентификатор пользователя в системе.
     * Генерируется автоматически при создании записи в базе данных.
     * Используется для однозначной идентификации пользователя в запросах.
     */
    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    /**
     * Логин пользователя, используемый для аутентификации.
     * Должен быть уникальным в пределах системы.
     * Отображается в интерфейсе и используется в логах для идентификации.
     */
    @Schema(description = "Логин пользователя", example = "ivan.ivanov")
    private String username;

    /**
     * Набор ролей, назначенных пользователю.
     * Определяет уровень доступа пользователя к различным частям системы.
     * Например: USER — обычный пользователь, ADMIN — администратор.
     */
    @Schema(description = "Роли пользователя в системе", example = "USER")
    private Set<UserRole> roles;
}