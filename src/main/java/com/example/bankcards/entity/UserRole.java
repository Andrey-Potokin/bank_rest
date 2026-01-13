package com.example.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;

/**
 * Перечисление ролей пользователей в системе.
 * <p>
 * Определяет уровни доступа и разрешённые действия для пользователей.
 * Реализует интерфейс {@link GrantedAuthority} для интеграции с Spring Security.
 * <p>
 * Поддерживаемые роли:
 * <ul>
 *   <li>{@link #USER} — обычный пользователь, может управлять своими картами</li>
 *   <li>{@link #ADMIN} — администратор, имеет полный доступ к системе</li>
 * </ul>
 */
@Schema(description = "Роль пользователя в системе")
public enum UserRole implements GrantedAuthority {

    /**
     * Роль обычного пользователя.
     * <p>
     * Пользователь с этой ролью может:
     * <ul>
     *   <li>Просматривать свои карты</li>
     *   <li>Блокировать свою карту</li>
     *   <li>Выполнять переводы между своими картами</li>
     * </ul>
     */
    @Schema(description = "Обычный пользователь")
    USER,

    /**
     * Роль администратора.
     * <p>
     * Администратор имеет расширенные права:
     * <ul>
     *   <li>Управление всеми пользователями (создание, изменение роли, удаление)</li>
     *   <li>Управление всеми картами (создание, активация, удаление)</li>
     *   <li>Просмотр списка всех карт в системе</li>
     * </ul>
     */
    @Schema(description = "Администратор")
    ADMIN;

    /**
     * Возвращает строковое представление роли, используемое Spring Security
     * для проверки доступа.
     * <p>
     * Например: "USER", "ADMIN".
     *
     * @return название роли в виде строки
     */
    @Override
    public String getAuthority() {
        return name();
    }
}