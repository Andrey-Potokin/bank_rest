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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/swagger-ui") || requestUri.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            log.warn("JWT Filter: Отсутствует заголовок Authorization. URI: {}, метод: {}", requestUri, request.getMethod());
            rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "Заголовок Authorization отсутствует");
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("JWT Filter: Неверный формат заголовка Authorization. Заголовок: {}, URI: {}", authHeader, requestUri);
            rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "Ожидается 'Bearer <токен>'");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            log.warn("JWT Filter: Токен пуст после удаления префикса Bearer. URI: {}", requestUri);
            rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "Токен пуст");
            return;
        }

        try {
            if (!jwtConfig.validateToken(token)) {
                log.error("JWT Filter: Проверка токена не пройдена. Токен: {}, URI: {}", token, requestUri);
                rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "Недействительный или просроченный токен");
                return;
            }

            String username = jwtConfig.getUsernameFromToken(token);
            if (username == null || username.isEmpty()) {
                log.warn("JWT Filter: Username не извлечён из токена. URI: {}", requestUri);
                rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "Некорректный токен");
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("JWT Filter: Аутентификация успешна. Пользователь: {}, URI: {}", username, requestUri);

        } catch (JWTVerificationException | IllegalArgumentException e) {
            log.error("JWT Filter: Ошибка проверки JWT. Сообщение: {}, Токен: {}, URI: {}",
                    e.getMessage(), token, requestUri, e);
            rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "Ошибка проверки токена");
            return;

        } catch (UsernameNotFoundException e) {
            log.error("JWT Filter: Пользователь не найден. URI: {}", requestUri);
            rejectRequest(response, HttpServletResponse.SC_UNAUTHORIZED, "Пользователь не найден");
            return;

        } catch (Exception e) {
            log.error("JWT Filter: Неожиданная ошибка. Сообщение: {}, URI: {}", e.getMessage(), requestUri, e);
            rejectRequest(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
            return;
        }

        chain.doFilter(request, response);
    }

    private void rejectRequest(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

}