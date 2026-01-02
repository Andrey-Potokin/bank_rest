package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    Page<CardDto> getUserCards(Long userId, Pageable pageable);

    Page<CardDto> getAllCards(Pageable pageable);

    CardDto createCard(CardDto cardDto, Long userId);

    void blockCard(Long cardId);

    void activateCard(Long cardId);

    void deleteCard(Long cardId);

    void transfer(Long fromCardId, Long toCardId, Double amount);

}