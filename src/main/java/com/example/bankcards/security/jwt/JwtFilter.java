package com.example.bankcards.security.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (
                requestUri.startsWith("/swagger-ui/") ||
                requestUri.startsWith("/v3/api-docs/") ||
                "/v3/api-docs.yaml".equals(requestUri)
        ) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            log.warn("Фильтр JWT: Отсутствует заголовок Authorization. URI запроса: {}, метод: {}",
                    request.getRequestURI(), request.getMethod());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Заголовок Authorization отсутствует");
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Фильтр JWT: Неверный формат заголовка Authorization. Заголовок: {}, URI запроса: {}",
                    authHeader, request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Неверный формат заголовка Authorization. Ожидается 'Bearer <токен>'");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            log.warn("Фильтр JWT: Токен пуст после удаления префикса Bearer. URI запроса: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Токен пуст");
            return;
        }

        try {
            if (jwtConfig.validateToken(token)) {
                String username = jwtConfig.getUsernameFromToken(token);

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("Фильтр JWT: Токен успешно проверен. Пользователь: {}, URI запроса: {}",
                        username, request.getRequestURI());
            } else {
                log.error("Фильтр JWT: Проверка токена не пройдена. Токен: {}, URI запроса: {}",
                        token, request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Недействительный или просроченный токен");
                return;
            }
        } catch (JWTVerificationException e) {
            log.error("Фильтр JWT: Ошибка проверки JWT. Сообщение: {}, Токен: {}, URI запроса: {}",
                    e.getMessage(), token, request.getRequestURI(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Ошибка проверки токена: " + e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            log.error("Фильтр JWT: Недопустимый аргумент при обработке токена. Сообщение: {}, Токен: {}, URI запроса: {}",
                    e.getMessage(), token, request.getRequestURI(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Некорректный формат токена: " + e.getMessage());
            return;
        } catch (Exception e) {
            log.error("Фильтр JWT: Неожиданная ошибка при аутентификации. Сообщение: {}, URI запроса: {}",
                    e.getMessage(), request.getRequestURI(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Внутренняя ошибка сервера при аутентификации");
            return;
        }

        chain.doFilter(request, response);
    }

}