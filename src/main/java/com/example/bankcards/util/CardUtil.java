package com.example.bankcards.util;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

/**
 * Утилитарный класс для преобразования данных между сущностью {@link Card}
 * и DTO-объектами ({@link CardResponse}, {@link CardCreateRequest}).
 * <p>
 * Содержит методы для:
 * <ul>
 *   <li>Преобразования сущности карты в DTO для ответа ({@link #toDto(Card)})</li>
 *   <li>Преобразования DTO запроса в сущность карты ({@link #toEntity(CardCreateRequest)})</li>
 *   <li>Маскирования номера карты для безопасного отображения ({@link #maskCardNumber(String)})</li>
 * </ul>
 * <p>
 * Класс объявлен как утилитарный с помощью {@link UtilityClass} из Lombok,
 * чтобы автоматически добавить закрытый конструктор и предотвратить создание экземпляров.
 */
@UtilityClass
public final class CardUtil {

    /**
     * Преобразует сущность банковской карты в DTO-объект для передачи в ответе API.
     * <p>
     * При преобразовании:
     * <ul>
     *   <li>Номер карты заменяется на замаскированный (например, "**** **** **** 4444")</li>
     *   <li>Срок действия форматируется в строку "ММ/ГГГГ"</li>
     *   <li>Копируются остальные поля: владелец, статус, баланс</li>
     * </ul>
     *
     * @param card сущность {@link Card}, не может быть null
     * @return объект {@link CardResponse} с данными карты, готовый к передаче клиенту
     */
    public static CardResponse toDto(Card card) {
        CardResponse dto = new CardResponse();
        dto.setId(card.getId());
        dto.setMaskedNumber(maskCardNumber(card.getNumber()));
        dto.setOwner(card.getOwner());

        if (card.getExpirationDate() != null) {
            String formattedDate = String.format("%02d/%d",
                    card.getExpirationDate().getMonthValue(),
                    card.getExpirationDate().getYear());
            dto.setExpirationDate(formattedDate);
        } else {
            dto.setExpirationDate(null);
        }

        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    /**
     * Преобразует DTO-объект запроса на создание карты в сущность {@link Card}.
     * <p>
     * При преобразовании:
     * <ul>
     *   <li>Номер, владелец и баланс копируются напрямую</li>
     *   <li>Срок действия из строки формата "ММ/ГГГГ" преобразуется в объект {@link LocalDate}</li>
     * </ul>
     *
     * @param dto объект {@link CardCreateRequest}, содержащий данные для создания карты; не может быть null
     * @return сущность {@link Card}, готовая к сохранению в базе данных
     */
    public static Card toEntity(CardCreateRequest dto) {
        Card card = new Card();
        card.setNumber(dto.getNumber());
        card.setOwner(dto.getOwner());

        if (dto.getExpirationDate() != null) {
            String[] parts = dto.getExpirationDate().split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            card.setExpirationDate(LocalDate.of(year, month, 1));
        }

        card.setBalance(dto.getBalance());
        return card;
    }

    /**
     * Маскирует номер банковской карты для безопасного отображения.
     * <p>
     * Заменяет первые 12 цифр на символы '*', оставляя последние 4 цифры открытыми.
     * Добавляет пробелы для форматирования по 4 цифры.
     * <p>
     * Пример:
     * <pre>
     * Вход:  "1111222233334444"
     * Выход: "**** **** **** 4444"
     * </pre>
     *
     * @param number исходный номер карты в формате "XXXX XXXX XXXX XXXX" или слитно; может быть null
     * @return замаскированный номер карты с пробелами; возвращает null, если номер равен null
     */
    private static String maskCardNumber(String number) {
        if (number == null) return null;
        String cleanNumber = number.replace(" ", "");
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);

        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < cleanNumber.length() - 4; i++) {
            masked.append('*');
        }
        masked.append(lastFour);

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < masked.length(); i++) {
            formatted.append(masked.charAt(i));
            if ((i + 1) % 4 == 0 && i != masked.length() - 1) {
                formatted.append(' ');
            }
        }

        return formatted.toString();
    }
}