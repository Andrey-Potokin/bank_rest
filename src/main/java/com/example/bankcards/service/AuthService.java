package com.example.bankcards.service;

/**
 * Сервис аутентификации пользователей.
 * <p>
 * Предоставляет методы для выполнения входа пользователя в систему
 * с проверкой учетных данных и генерацией JWT-токена.
 */
public interface AuthService {

    /**
     * Выполняет аутентификацию пользователя по логину и паролю.
     * <p>
     * Проверяет наличие пользователя с указанным логином, корректность пароля
     * и активность учётной записи. При успешной аутентификации генерирует
     * и возвращает JWT-токен, который может быть использован для доступа
     * к защищённым ресурсам системы.
     *
     * @param username логин пользователя, не может быть null или пустым
     * @param password пароль пользователя в открытом виде, не может быть null или пустым
     * @return строка с JWT-токеном в формате Bearer, если аутентификация прошла успешно
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         если переданы неверные логин или пароль
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException
     *         если пользователь с указанным логином не найден
     */
    String login(String username, String password);
}