package com.example.bankcards.security;

import com.example.bankcards.config.JwtConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Фильтр аутентификации на основе JWT (JSON Web Token).
 * <p>
 * Этот фильтр перехватывает каждый HTTP-запрос к защищённым эндпоинтам, извлекает
 * токен из заголовка {@code Authorization}, проверяет его валидность и, если токен корректен,
 * устанавливает аутентифицированного пользователя в контекст Spring Security.
 * <p>
 * Для публичных маршрутов (например, вход или Swagger) фильтр пропускает запрос дальше,
 * не требуя токена.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /**
     * Конфигурация JWT, отвечающая за валидацию токена и извлечение данных.
     */
    private final JwtConfig jwtConfig;

    /**
     * Утилита для сопоставления путей с шаблонами (например, "/api/auth/**").
     * Используется для определения, нужно ли обрабатывать запрос этим фильтром.
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Основной метод фильтра, вызываемый для каждого входящего HTTP-запроса.
     * <p>
     * Логика работы:
     * <ul>
     *   <li>Если путь соответствует публичным маршрутам (вход, Swagger), запрос пропускается дальше</li>
     *   <li>Если заголовок Authorization отсутствует или не начинается с "Bearer ", возвращается 401</li>
     *   <li>Иначе извлекается токен, проверяется его валидность и устанавливается аутентификация</li>
     * </ul>
     *
     * @param request     объект HTTP-запроса
     * @param response    объект HTTP-ответа
     * @param filterChain цепочка фильтров, через которую передаётся запрос
     * @throws ServletException если произошла ошибка сервлета
     * @throws IOException      если произошла ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Пропускаем публичные эндпоинты без проверки токена
        if (pathMatcher.match("/api/auth/**", requestPath) ||
                pathMatcher.match("/swagger-ui/**", requestPath) ||
                pathMatcher.match("/v3/api-docs/**", requestPath) ||
                pathMatcher.match("/v3/api-docs.yaml", requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Извлечение заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Извлечение токена (удаление префикса "Bearer ")
        String token = authHeader.substring(7);
        try {
            // Валидация токена
            jwtConfig.validateToken(token);

            // Извлечение имени пользователя и ролей из токена
            String username = jwtConfig.getUsernameFromToken(token);
            List<String> roles = jwtConfig.getRolesFromToken(token);

            // Преобразование ролей в объекты GrantedAuthority
            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Создание аутентификационного токена
            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

            // Установка аутентификации в контекст Spring Security
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Передача запроса дальше по цепочке фильтров
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // При любой ошибке — возвращаем 401 и сообщение
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Невалидный или просроченный JWT");
        }
    }
}