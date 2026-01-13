package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

/**
 * REST-контроллер для управления банковскими картами от имени администратора.
 * <p>
 * Обеспечивает операции по просмотру, созданию, активации и удалению карт.
 * Доступ к методам контроллера разрешен только пользователям с ролью ADMIN.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/cards")
@Tag(name = "Админ: Карты", description = "API для управления картами (только ADMIN)")
public class AdminCardController {

    private final CardService cardService;

    /**
     * Получает список всех банковских карт с поддержкой пагинации.
     * <p>
     * Возвращает страницу с информацией о картах, соответствующих текущему запросу.
     * Используется для административного просмотра всех карт в системе.
     *
     * @param pageable параметры пагинации (номер страницы, размер, сортировка)
     * @return ResponseEntity с объектом Page, содержащим список {@link CardResponse}
     *         и HTTP-статус 200 (OK)
     */
    @GetMapping
    @Operation(summary = "Получить все карты с пагинацией")
    @ApiResponse(
            responseCode = "200",
            description = "Список карт",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
    )
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (не ADMIN)")
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        log.info("ADMIN: Получение всех карт. Страница={}, размер={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<CardResponse> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Создаёт новую банковскую карту для указанного пользователя.
     * <p>
     * Принимает данные для создания карты и идентификатор пользователя.
     * Проверка корректности данных выполняется с помощью валидации.
     *
     * @param request объект с данными для создания карты, не может быть null
     * @param userId идентификатор пользователя, которому выдаётся карта, должен быть положительным числом
     * @return ResponseEntity с объектом {@link CardResponse}, содержащим данные созданной карты,
     *         и HTTP-статус 201 (Created)
     */
    @PostMapping
    @Operation(summary = "Создать новую карту")
    @ApiResponse(responseCode = "201", description = "Карта создана")
    @ApiResponse(responseCode = "400", description = "Некорректные данные карты")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CardCreateRequest request,
            @RequestParam("userId") @Positive Long userId) {

        log.info("ADMIN: Создание карты для пользователя ID={}", userId);
        CardResponse createdCard = cardService.createCard(request, userId);
        log.info("Карта создана с ID={} для пользователя ID={}", createdCard.getId(), userId);
        return ResponseEntity.created(null).body(createdCard);
    }

    /**
     * Активирует существующую банковскую карту.
     * <p>
     * После активации карта становится пригодной для использования.
     * Выполняется проверка существования карты по идентификатору.
     *
     * @param cardId идентификатор карты, которую необходимо активировать
     * @return ResponseEntity с пустым телом и HTTP-статусом 200 (OK)
     */
    @PutMapping("/{cardId}/activate")
    @Operation(summary = "Активировать карту")
    @ApiResponse(responseCode = "200", description = "Карта активирована")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    public ResponseEntity<Void> activateCard(@PathVariable Long cardId) {
        log.info("ADMIN: Активация карты ID={}", cardId);
        cardService.activateCard(cardId);
        log.info("Карта ID={} активирована", cardId);
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет банковскую карту из системы.
     * <p>
     * Операция безвозвратно удаляет карту по её идентификатору.
     * Может использоваться для удаления ошибочно созданных или утерянных карт.
     *
     * @param cardId идентификатор карты, которую необходимо удалить
     * @return ResponseEntity с пустым телом и HTTP-статусом 204 (No Content)
     */
    @DeleteMapping("/{cardId}")
    @Operation(summary = "Удалить карту")
    @ApiResponse(responseCode = "204", description = "Карта удалена")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        log.info("ADMIN: Удаление карты ID={}", cardId);
        cardService.deleteCard(cardId);
        log.info("Карта ID={} удалена", cardId);
        return ResponseEntity.noContent().build();
    }
}