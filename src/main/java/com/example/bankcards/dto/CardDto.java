package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления банковской карты")
public class CardDto {

    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Номер банковской карты", example = "1111 2222 3333 4444")
    @NotBlank(message = "Номер карты не может быть пустым или состоять только из пробелов")
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$",
            message = "Номер карты должен быть в формате 'XXXX XXXX XXXX XXXX' (четыре группы по 4 цифры через пробел)")
    private String maskedNumber;

    @Schema(description = "Владелец карты", example = "Иван Иванов")
    @NotBlank(message = "Имя владельца карты не может быть пустым или состоять только из пробелов")
    @Pattern(regexp = "^[а-яА-ЯёЁ\\s]+$",
            message = "Имя владельца может содержать только кириллические буквы и пробелы")
    private String owner;

    @Schema(description = "Срок действия карты", example = "12/2025")
    @NotBlank(message = "Срок действия карты не может быть пустым")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/\\d{4}$",
            message = "Срок действия должен быть в формате ММ/ГГГГ (например, 12/2025)")
    private String expirationDate;

    @Schema(
            description = "Статус карты",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"},
            implementation = CardStatus.class
    )
    @NotNull(message = "Статус карты обязателен")
    private CardStatus status;

    @Schema(description = "Баланс карты", type = "number", example = "1000.50")
    @PositiveOrZero(message = "Баланс не может быть отрицательным числом")
    private Double balance;

}