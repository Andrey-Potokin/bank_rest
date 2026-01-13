package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для работы с банковскими картами от имени пользователя.
 * <p>
 * Предоставляет функциональность просмотра своих карт, блокировки карты
 * и перевода средств между собственными картами.
 * Доступ к методам контроллера разрешён только пользователям с ролью USER.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/cards")
@Tag(name = "Пользователь: Карты", description = "API для работы с картами пользователя (только USER)")
public class UserCardController {

    /**
     * Сервис для работы с банковскими картами.
     * Обеспечивает бизнес-логику получения, блокировки и перевода средств между картами.
     */
    private final CardService cardService;

    /**
     * Получает страницу с банковскими картами пользователя.
     * <p>
     * Выполняет проверку, что идентификатор пользователя положительный.
     * Возвращает список карт с поддержкой пагинации (размер страницы, номер, сортировка).
     *
     * @param userId   идентификатор пользователя, чьи карты запрашиваются; должен быть положительным
     * @param pageable параметры пагинации: номер страницы, размер, сортировка
     * @return ResponseEntity с объектом {@link Page} содержащим список {@link CardResponse},
     *         с HTTP-статусом 200 (OK)
     * @throws IllegalArgumentException если userId не положительный
     */
    @GetMapping
    @Operation(summary = "Получить свои карты с пагинацией")
    @ApiResponse(responseCode = "200", description = "Список карт пользователя")
    @ApiResponse(responseCode = "400", description = "Некорректный ID пользователя")
    public ResponseEntity<Page<CardResponse>> getUserCards(
            @RequestParam("userId") Long userId,
            Pageable pageable) {

        if (userId <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        log.info("USER: Запрос карт для ID={}. Страница={}, размер={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<CardResponse> cards = cardService.getUserCards(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Блокирует указанную банковскую карту.
     * <p>
     * Проверяет, что карта принадлежит текущему пользователю.
     * После успешной блокировки карта становится недоступной для операций.
     *
     * @param cardId идентификатор карты, которую необходимо заблокировать
     * @return ResponseEntity с пустым телом и HTTP-статусом 200 (OK)
     */
    @PostMapping("/{cardId}/block")
    @Operation(summary = "Заблокировать свою карту")
    @ApiResponse(responseCode = "200", description = "Карта заблокирована")
    @ApiResponse(responseCode = "403", description = "Карта не принадлежит пользователю")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    public ResponseEntity<Void> blockCard(@PathVariable Long cardId) {
        log.info("USER: Попытка блокировки карты ID={}", cardId);
        cardService.blockCard(cardId);
        log.info("Карта ID={} успешно заблокирована", cardId);
        return ResponseEntity.ok().build();
    }

    /**
     * Выполняет перевод средств между двумя картами пользователя.
     * <p>
     * Проверяет, что обе карты принадлежат одному пользователю, достаточность средств
     * на счёт-отправителя и корректность суммы перевода.
     *
     * @param fromCardId идентификатор карты-отправителя
     * @param toCardId   идентификатор карты-получателя
     * @param amount     сумма перевода, должна быть положительной
     * @return ResponseEntity с пустым телом и HTTP-статусом 200 (OK)
     */
    @PostMapping("/transfer")
    @Operation(summary = "Перевести средства между своими картами")
    @ApiResponse(responseCode = "200", description = "Перевод выполнен")
    @ApiResponse(responseCode = "400", description = "Некорректная сумма или ID карт")
    @ApiResponse(responseCode = "403", description = "Карты не принадлежат пользователю")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @ApiResponse(responseCode = "409", description = "Недостаточно средств")
    public ResponseEntity<Void> transfer(
            @RequestParam("fromCardId") Long fromCardId,
            @RequestParam("toCardId") Long toCardId,
            @RequestParam("amount") @Positive Double amount) {

        log.info("USER: Перевод: с карты {} на карту {}, сумма={}", fromCardId, toCardId, amount);

        cardService.transfer(fromCardId, toCardId, amount);

        log.info("Перевод выполнен: {} руб. с карты {} на карту {}", amount, fromCardId, toCardId);
        return ResponseEntity.ok().build();
    }
}