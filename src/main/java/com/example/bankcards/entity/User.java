package com.example.bankcards.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * Сущность пользователя системы.
 * <p>
 * Описывает данные пользователя, необходимые для аутентификации и авторизации.
 * Реализует интерфейс {@link UserDetails} из Spring Security, что позволяет
 * использовать объект в механизме управления доступом.
 * <p>
 * Каждый пользователь имеет уникальный логин, зашифрованный пароль и набор ролей.
 * Роли хранятся в отдельной таблице {@code user_roles} и загружаются сразу при
 * получении пользователя.
 *
 * @see UserDetails
 * @see UserRole
 */
@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    /**
     * Уникальный идентификатор пользователя в системе.
     * Генерируется автоматически с использованием стратегии IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Логин пользователя, используемый для входа в систему.
     * Должен быть уникальным, не может быть пустым.
     * Хранится в столбце "username" таблицы "users".
     */
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    /**
     * Хэш пароля пользователя.
     * Хранится в зашифрованном виде (например, с использованием BCrypt).
     * Не может быть пустым.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Набор ролей, назначенных пользователю.
     * <p>
     * Хранится в отдельной таблице {@code user_roles} в виде строк (например: USER, ADMIN).
     * Загружается сразу (EAGER), чтобы избежать проблем с доступом к ролям после завершения сессии.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    /**
     * Возвращает коллекцию полномочий (ролей) пользователя, необходимых для Spring Security.
     * <p>
     * Каждая роль преобразуется в объект {@link SimpleGrantedAuthority} с префиксом "ROLE_".
     * Например: USER → ROLE_USER, ADMIN → ROLE_ADMIN.
     *
     * @return коллекция объектов {@link GrantedAuthority}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }
}