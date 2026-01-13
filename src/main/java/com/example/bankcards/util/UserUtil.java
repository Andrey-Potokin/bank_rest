package com.example.bankcards.util;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import lombok.experimental.UtilityClass;

/**
 * Утилитарный класс для преобразования данных между сущностью {@link User} и DTO.
 * <p>
 * Содержит статические методы для:
 * <ul>
 *   <li>Преобразования сущности пользователя в DTO-объект {@link UserResponse} для передачи клиенту</li>
 *   <li>Преобразования DTO-объекта {@link UserCreateRequest} в сущность пользователя для сохранения в базе данных</li>
 * </ul>
 * <p>
 * Класс помечен аннотацией {@link UtilityClass} из Lombok, что предотвращает создание экземпляров
 * и автоматически генерирует закрытый конструктор.
 */
@UtilityClass
public final class UserUtil {

    /**
     * Преобразует сущность пользователя в DTO-объект для ответа API.
     * <p>
     * Копирует идентификатор, логин и набор ролей из сущности {@link User} в объект {@link UserResponse}.
     * Используется при возврате данных о пользователе в контроллерах.
     *
     * @param user сущность пользователя, не может быть null
     * @return объект {@link UserResponse}, содержащий ID, логин и роли пользователя
     */
    public static UserResponse toDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }

    /**
     * Преобразует DTO-объект запроса создания пользователя в сущность {@link User}.
     * <p>
     * Копирует логин из объекта {@link UserCreateRequest} в сущность пользователя.
     * Другие поля (пароль, роли) устанавливаются отдельно при создании пользователя.
     *
     * @param dto объект DTO с данными для создания пользователя, не может быть null
     * @return сущность {@link User} с заполненным логином, готовая для дальнейшего заполнения и сохранения
     */
    public static User toEntity(UserCreateRequest dto) {
        return User.builder()
                .username(dto.getUsername())
                .build();
    }
}