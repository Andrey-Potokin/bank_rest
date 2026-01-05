package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    Page<CardResponse> getUserCards(Long userId, Pageable pageable);

    Page<CardResponse> getAllCards(Pageable pageable);

    CardResponse createCard(CardCreateRequest request, Long userId);

    void blockCard(Long cardId);

    void activateCard(Long cardId);

    void deleteCard(Long cardId);

    void transfer(Long fromCardId, Long toCardId, Double amount);

}