package com.example.bankcards.service;

import com.example.bankcards.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса аутентификации пользователей.
 * <p>
 * Предоставляет функциональность для входа пользователя в систему
 * с проверкой учётных данных и генерацией JWT-токена при успешной аутентификации.
 * <p>
 * Сервис использует стандартный механизм аутентификации Spring Security через
 * {@link AuthenticationManager} и генерирует токен с помощью {@link JwtConfig}.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * Конфигурация JWT, отвечающая за генерацию и валидацию токенов.
     * Используется для создания токена после успешной аутентификации.
     */
    private final JwtConfig jwtConfig;

    /**
     * Менеджер аутентификации Spring Security.
     * Выполняет проверку логина и пароля с использованием настроенных провайдеров
     * (например, через {@link com.example.bankcards.security.UserDetailsServiceImpl}).
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Выполняет аутентификацию пользователя по логину и паролю и генерирует JWT-токен.
     * <p>
     * Создаёт токен аутентификации с указанными учётными данными, передаёт его
     * {@link AuthenticationManager} для проверки. При успешной проверке извлекает
     * аутентифицированного пользователя и генерирует для него JWT-токен.
     *
     * @param username логин пользователя; не может быть null или пустым
     * @param password пароль пользователя в открытом виде; не может быть null или пустым
     * @return строка с JWT-токеном в формате Bearer, пригодная для использования
     *         в заголовке Authorization последующих запросов
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         если переданы неверные логин или пароль
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException
     *         если пользователь с указанным логином не найден в системе
     */
    @Override
    public String login(String username, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authToken);

        return jwtConfig.generateToken(authentication.getPrincipal());
    }
}