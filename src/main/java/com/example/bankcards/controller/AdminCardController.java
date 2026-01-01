package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/cards")
@Tag(name = "Админ: Карты", description = "API для управления картами (только ADMIN)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Получить все карты с пагинацией")
    @ApiResponse(
            responseCode = "200",
            description = "Список карт",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
    )
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (не ADMIN)")
    public ResponseEntity<Page<CardDto>> getAllCards(Pageable pageable) {
        log.info("ADMIN: Получение всех карт. Страница={}, размер={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<CardDto> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @Operation(summary = "Создать новую карту")
    @ApiResponse(responseCode = "201", description = "Карта создана")
    @ApiResponse(responseCode = "400", description = "Некорректные данные карты")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<CardDto> createCard(
            @Valid @RequestBody CardDto cardDto,
            @RequestParam("userId") Long userId) {

        log.info("ADMIN: Создание карты для пользователя ID={}", userId);
        CardDto createdCard = cardService.createCard(cardDto, userId);
        log.info("Карта создана с ID={} для пользователя ID={}", createdCard.getId(), userId);
        return ResponseEntity.created(null).body(createdCard);
    }

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