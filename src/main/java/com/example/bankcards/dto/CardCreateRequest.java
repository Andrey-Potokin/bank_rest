package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для передачи данных при создании новой банковской карты.
 * <p>
 * Используется в {@link com.example.bankcards.controller.AdminCardController} для
 * получения параметров новой карты от администратора.
 * <p>
 * Объект содержит информацию о номере карты, владельце, сроке действия и начальном балансе.
 * Все поля проходят строгую валидацию на соответствие формату и допустимым значениям.
 */
@Data
@Builder
@Schema(description = "DTO для создания банковской карты")
public class CardCreateRequest {

    /**
     * Номер банковской карты в формате четырёх групп по 4 цифры, разделённых пробелами.
     * <p>
     * Пример: "1111 2222 3333 4444"
     * <p>
     * Поле обязательно для заполнения и должно соответствовать регулярному выражению:
     * {@code ^\d{4} \d{4} \d{4} \d{4}$}
     */
    @Schema(description = "Номер банковской карты", example = "1111 2222 3333 4444")
    @NotBlank(message = "Номер карты не может быть пустым или состоять только из пробелов")
    @Pattern(regexp = "^\\d{4} \\d{4} \\d{4} \\d{4}$",
            message = "Номер карты должен быть в формате 'XXXX XXXX XXXX XXXX' (четыре группы по 4 цифры через пробел)")
    private String number;

    /**
     * ФИО владельца карты.
     * <p>
     * Должно содержать только кириллические символы и пробелы.
     * Пример: "Иван Иванов"
     * <p>
     * Поле обязательно для заполнения.
     */
    @Schema(description = "Владелец карты", example = "Иван Иванов")
    @NotBlank(message = "Имя владельца карты не может быть пустым или состоять только из пробелов")
    @Pattern(regexp = "^[а-яА-ЯёЁ\\s]+$",
            message = "Имя владельца может содержать только кириллические буквы и пробелы")
    private String owner;

    /**
     * Срок действия карты в формате ММ/ГГГГ.
     * <p>
     * Пример: "12/2025"
     * <p>
     * Поле обязательно для заполнения и должно соответствовать регулярному выражению:
     * {@code ^(0[1-9]|1[0-2])/\d{4}$}
     */
    @Schema(description = "Срок действия карты", example = "12/2025")
    @NotBlank(message = "Срок действия карты не может быть пустым")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/\\d{4}$",
            message = "Срок действия должен быть в формате ММ/ГГГГ (например, 12/2025)")
    private String expirationDate;

    /**
     * Начальный баланс карты в рублях.
     * <p>
     * Значение должно быть неотрицательным (ноль или больше).
     * Пример: 1000.50
     * <p>
     * Используется при создании дебетовой карты.
     */
    @Schema(description = "Баланс карты", type = "number", example = "1000.50")
    @PositiveOrZero(message = "Баланс не может быть отрицательным числом")
    private Double balance;
}