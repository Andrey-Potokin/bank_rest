package com.example.bankcards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для настройки OpenAPI (Swagger) документации.
 * <p>
 * Обеспечивает:
 * <ul>
 *   <li>Общее описание API: название, версия, описание</li>
 *   <li>Группировку эндпоинтов по ролям: auth, admin, user</li>
 *   <li>Настройку схемы аутентификации JWT (Bearer)</li>
 * </ul>
 
 * <p>
 * Важно: требование аутентификации применяется только к защищённым группам.
 * Эндпоинт {@code /api/auth/login} остаётся публичным и не требует токена.
 
 * <p>
 * Документация доступна по адресу: <a href="http://localhost:8080/swagger-ui/index.html">http://localhost:8080/swagger-ui/index.html</a>
 
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создаёт основной объект OpenAPI с общей информацией о приложении.
     * <p>
     * Не добавляет глобальное требование безопасности, чтобы избежать
     * пометки публичных эндпоинтов (например, /login) как защищённых.
     * Схема безопасности определяется только в нужных группах.
     
     *
     * @return настроенный объект {@link OpenAPI}
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Cards API")
                        .version("1.0")
                        .description("API для управления банковскими картами и пользователями"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", securityScheme()));
    }

    /**
     * Определяет схему безопасности для аутентификации через JWT.
     * <p>
     * Используется метод аутентификации HTTP Bearer.
     * Формат токена — JWT.
     * Имя схемы — "bearerAuth", используется в группах {@code admin} и {@code user}.
     
     *
     * @return объект {@link SecurityScheme}, описывающий метод аутентификации
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("bearerAuth");
    }

    /**
     * Группа эндпоинтов, связанных с аутентификацией.
     * <p>
     * Включает:
     * <ul>
     *   <li>{@code POST /api/auth/login} — вход пользователя</li>
     * </ul>
     
     * <p>
     * Эта группа <b>не требует токена</b> и отображается в Swagger UI без значка замка.
     * Это позволяет тестировать вход без предварительной аутентификации.
     
     *
     * @return объект {@link GroupedOpenApi} для публичных эндпоинтов
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * Группа эндпоинтов для администраторов.
     * <p>
     * Включает:
     * <ul>
     *   <li>{@code /api/admin/users/**} — управление пользователями</li>
     *   <li>{@code /api/admin/cards/**} — управление картами</li>
     * </ul>
     
     * <p>
     * Все эндпоинты этой группы помечаются в Swagger UI как требующие Bearer-токен.
     * Доступ разрешён только пользователям с ролью ADMIN.
     
     *
     * @return объект {@link GroupedOpenApi} с настроенной безопасностью
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                    return operation;
                })
                .build();
    }

    /**
     * Группа эндпоинтов для обычных пользователей.
     * <p>
     * Включает:
     * <ul>
     *   <li>{@code /api/user/cards/**} — работа с собственными картами</li>
     * </ul>
     * <p>
     * Все эндпоинты этой группы требуют Bearer-токена.
     * Доступ разрешён только авторизованным пользователям (роль USER).
     *
     * @return объект {@link GroupedOpenApi} с настроенной безопасностью
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .pathsToMatch("/api/user/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                    return operation;
                })
                .build();
    }
}