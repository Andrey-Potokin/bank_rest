package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/cards")
@Tag(name = "Пользователь: Карты", description = "API для работы с картами пользователя (только USER)")
@PreAuthorize("hasRole('USER')")
public class UserCardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Получить свои карты с пагинацией")
    @ApiResponse(responseCode = "200", description = "Список карт пользователя")
    @ApiResponse(responseCode = "400", description = "Некорректный ID пользователя")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @RequestParam("userId") Long userId,
            Pageable pageable) {

        if (userId <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        log.info("USER: Запрос карт для ID={}. Страница={}, размер={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<CardDto> cards = cardService.getUserCards(userId, pageable);
        return ResponseEntity.ok(cards);
    }

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