package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для обработки аутентификации пользователей.
 * <p>
 * Предоставляет конечную точку для входа пользователя в систему
 * с получением JWT-токена в ответ при успешной аутентификации.
 * Контроллер доступен без аутентификации и предназначен для всех пользователей.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для аутентификации пользователей")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Сервис аутентификации, отвечающий за логику входа и генерацию JWT-токенов.
     */
    private final AuthService authService;

    /**
     * Выполняет аутентификацию пользователя по логину и паролю.
     * <p>
     * Принимает данные для входа, проверяет их корректность и,
     * при успешной проверке, возвращает JWT-токен.
     *
     * @param request объект с данными для аутентификации (логин и пароль), не может быть null
     * @return ResponseEntity с объектом {@link JwtResponse}, содержащим JWT-токен,
     *         и HTTP-статусом 200 (OK)
     */
    @PostMapping("/login")
    @Operation(summary = "Вход пользователя")
    @ApiResponse(responseCode = "200", description = "Успешная аутентификация")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody AuthRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());

        log.info("Пользователь успешно аутентифицирован: username={}", request.getUsername());

        return ResponseEntity.ok(new JwtResponse(token));
    }
}