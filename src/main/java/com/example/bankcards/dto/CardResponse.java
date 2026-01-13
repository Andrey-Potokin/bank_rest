package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о банковской карте в ответе API.
 * <p>
 * Содержит основную информацию о карте, включая идентификатор, замаскированный номер,
 * владельца, срок действия, статус и текущий баланс. Используется контроллерами
 * и сервисами для возврата данных клиенту.
 * <p>
 * Замаскированный номер (maskedNumber) скрывает первые 12 цифр для безопасности.
 * Например: "**** **** **** 4444".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для ответа с данными банковской карты")
public class CardResponse {

    /**
     * Уникальный идентификатор банковской карты в системе.
     * Генерируется автоматически при создании записи в базе данных.
     */
    @Schema(description = "ID карты", example = "1")
    private Long id;

    /**
     * Номер банковской карты с замаскированными первыми 12 цифрами.
     * Отображается в формате: "**** **** **** XXXX".
     * Предназначен для безопасного отображения пользователю.
     */
    @Schema(description = "Номер банковской карты (замаскированный)", example = "**** **** **** 4444")
    private String maskedNumber;

    /**
     * ФИО владельца карты.
     * Должно соответствовать данным, указанным при создании карты.
     */
    @Schema(description = "Владелец карты", example = "Иван Иванов")
    private String owner;

    /**
     * Срок действия карты в формате ММ/ГГГГ.
     * После окончания срока карта блокируется автоматически.
     */
    @Schema(description = "Срок действия карты", example = "12/2025")
    private String expirationDate;

    /**
     * Текущий статус карты: ACTIVE, BLOCKED, EXPIRED и т.д.
     * Определяет, может ли карта использоваться в операциях.
     *
     * @see CardStatus
     */
    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    /**
     * Текущий баланс карты в рублях.
     * Может быть нулевым или положительным.
     * Отрицательные значения не допускаются.
     */
    @Schema(description = "Баланс карты", type = "number", example = "1000.50")
    private Double balance;
}