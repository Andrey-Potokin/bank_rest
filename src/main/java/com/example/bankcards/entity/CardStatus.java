package com.example.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Статус карты")
public enum CardStatus {
    @Schema(description = "Активна")
    ACTIVE("Активна"),
    @Schema(description = "Заблокирована")
    BLOCKED("Заблокирована"),
    @Schema(description = "Срок истёк")
    EXPIRED("Срок истёк");

    @Schema(description = "Текстовое представление статуса")
    private final String value;

    CardStatus(String value) {
        this.value = value;
    }
}