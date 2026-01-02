package com.example.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;

@Schema(
        description = "Роль пользователя в системе",
        implementation = String.class,
        allowableValues = {"USER", "ADMIN"}
)
public enum UserRole implements GrantedAuthority {

    @Schema(description = "Обычный пользователь")
    USER,

    @Schema(description = "Администратор")
    ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }

}