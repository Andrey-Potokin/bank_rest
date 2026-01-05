package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Tag(name = "Админ: Пользователи", description = "API для управления пользователями (только ADMIN)")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создать нового пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь создан")
    @ApiResponse(responseCode = "400", description = "Логин занят или некорректные данные")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @RequestParam("password") @NotBlank String password) {

        log.info("ADMIN: Создание пользователя с username={}", request.getUsername());
        UserResponse createdUser = userService.createUser(request, password);
        log.info("Пользователь создан: ID={}, username={}", createdUser.getId(), createdUser.getUsername());
        return ResponseEntity.created(null).body(createdUser);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Получить пользователя по ID")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        log.info("ADMIN: Запрос пользователя ID={}", userId);
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Обновить роль пользователя")
    @ApiResponse(responseCode = "200", description = "Роль обновлена")
    @ApiResponse(responseCode = "400", description = "Недопустимая роль")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long userId,
            @RequestParam("role") @NotBlank String role) {

        log.info("ADMIN: Обновление роли пользователя ID={} на {}", userId, role);
        userService.updateRole(userId, role);
        log.info("Роль пользователя ID={} обновлена на {}", userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Удалить пользователя")
    @ApiResponse(responseCode = "204", description = "Пользователь удалён")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("ADMIN: Удаление пользователя ID={}", userId);
        userService.deleteUser(userId);
        log.info("Пользователь ID={} удалён", userId);
        return ResponseEntity.noContent().build();
    }

}