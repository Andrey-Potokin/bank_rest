package com.example.bankcards.config;

import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.example.bankcards.entity.UserRole.ADMIN;
import static com.example.bankcards.entity.UserRole.USER;

/**
 * Конфигурация безопасности приложения с использованием Spring Security.
 * <p>
 * Настраивает:
 * <ul>
 *   <li>Аутентификацию через JWT</li>
 *   <li>Авторизацию по ролям: USER и ADMIN</li>
 *   <li>Отключение CSRF (так как используется stateless аутентификация)</li>
 *   <li>Настройку CORS для доступа с фронтенда</li>
 *   <li>Безсессионный режим (STATELESS)</li>
 * </ul>
 * <p>
 * Использует кастомный фильтр {@link JwtFilter} для проверки JWT-токена в заголовке Authorization.
 * Аутентификация пользователей выполняется через {@link UserDetailsServiceImpl}.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Фильтр JWT, перехватывающий запросы и проверяющий валидность JWT-токена.
     * Добавляется в цепочку фильтров перед {@link UsernamePasswordAuthenticationFilter}.
     */
    private final JwtFilter jwtFilter;

    /**
     * Сервис загрузки данных пользователя для аутентификации.
     * Реализует интерфейс {@link org.springframework.security.core.userdetails.UserDetailsService}.
     */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Определяет цепочку фильтров безопасности.
     * <p>
     * Настройки:
     * <ul>
     *   <li>Отключён CSRF</li>
     *   <li>Включён CORS с настройками из {@link #corsConfigurationSource()}</li>
     *   <li>Разрешён доступ:
     *     <ul>
     *       <li>К /api/auth/** — всем (регистрация, вход)</li>
     *       <li>К Swagger — всем</li>
     *       <li>К /api/admin/** — только с ролью ADMIN</li>
     *       <li>К /api/user/** — только с ролью USER</li>
     *       <li>Остальные запросы — только аутентифицированным</li>
     *     </ul>
     *   </li>
     *   <li>Добавлен {@link JwtFilter} перед стандартной аутентификацией</li>
     *   <li>Режим сессии — STATELESS</li>
     * </ul>
     *
     * @param http объект {@link HttpSecurity} для настройки безопасности HTTP
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception если произойдёт ошибка при настройке
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/api/admin/**").hasRole(ADMIN.getAuthority())
                        .requestMatchers("/api/user/**").hasRole(USER.getAuthority())
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationManager(authenticationManager(null));

        return http.build();
    }

    /**
     * Возвращает энкодер паролей на основе алгоритма BCrypt.
     * <p>
     * Используется для безопасного хранения паролей в базе данных.
     *
     * @return экземпляр {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Возвращает менеджер аутентификации Spring Security.
     * <p>
     * Используется {@link com.example.bankcards.service.AuthServiceImpl} для проверки логина и пароля пользователя
     * при выполнении операции входа.
     *
     * @param authConfig конфигурация аутентификации (внедряется автоматически)
     * @return экземпляр {@link AuthenticationManager}
     * @throws Exception если конфигурация недоступна
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Возвращает провайдер аутентификации, использующий кастомный {@link UserDetailsServiceImpl}
     * и энкодер паролей.
     * <p>
     * Может быть расширен для поддержки других способов аутентификации.
     *
     * @return экземпляр {@link DaoAuthenticationProvider}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Настройка CORS (Cross-Origin Resource Sharing).
     * <p>
     * Разрешает запросы с:
     * <ul>
     *   <li>http://localhost:8080</li>
     *   <li>http://127.0.0.1:8080</li>
     * </ul>
     * Поддерживает методы: GET, POST, PUT, DELETE, OPTIONS.
     * Разрешены все заголовки. Включены учётные данные (cookies, авторизация).
     * Максимальное время кэширования preflight-запроса — 300 секунд.
     *
     * @return источник конфигурации CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:8080",
                "http://127.0.0.1:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(300L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}