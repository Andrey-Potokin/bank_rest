package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для ответа с данными банковской карты")
public class CardResponse {

    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Номер банковской карты (замаскированный)", example = "**** **** **** 4444")
    private String maskedNumber;

    @Schema(description = "Владелец карты", example = "Иван Иванов")
    private String owner;

    @Schema(description = "Срок действия карты", example = "12/2025")
    private String expirationDate;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс карты", type = "number", example = "1000.50")
    private Double balance;

}