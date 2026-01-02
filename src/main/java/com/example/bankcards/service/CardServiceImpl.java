package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidAmountException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.TransferNotAllowedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getUserCards(Long userId, Pageable pageable) {
        if (userId <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }

        Page<Card> cardsPage = cardRepository.findByUserId(userId, pageable);
        List<CardDto> cardDtos = cardsPage.getContent().stream()
                .map(CardUtil::toDto)
                .toList();

        return new PageImpl<>(cardDtos, pageable, cardsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(CardUtil::toDto);
    }

    @Override
    @Transactional
    public CardDto createCard(CardDto cardDto, Long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("ID пользователя должен быть положительным");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден"));

        Card card = CardUtil.toEntity(cardDto);
        card.setUser(user);
        Card savedCard = cardRepository.save(card);

        log.info("Создана карта ID={} для пользователя ID={}", savedCard.getId(), userId);
        return CardUtil.toDto(savedCard);
    }

    @Override
    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID=" + cardId + " не найдена"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (!card.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Карта не принадлежит текущему пользователю");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("Карта ID={} заблокирована пользователем {}", cardId, currentUsername);
    }

    @Override
    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID=" + cardId + " не найдена"));

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        log.info("Карта ID={} активирована", cardId);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID=" + cardId + " не найдена"));

        cardRepository.delete(card);
        log.info("Карта ID={} удалена", cardId);
    }

    @Override
    @Transactional
    public void transfer(Long fromCardId, Long toCardId, Double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Сумма перевода должна быть положительной");
        }

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Исходная карта с ID=" + fromCardId + " не найдена"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new NotFoundException("Целевая карта с ID=" + toCardId + " не найдена"));

        if (!fromCard.getUser().equals(toCard.getUser())) {
            throw new TransferNotAllowedException(
                    "Перевод между картами разных пользователей запрещён");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (!currentUsername.equals(fromCard.getUser().getUsername()) ||
                !currentUsername.equals(toCard.getUser().getUsername())) {
            throw new AccessDeniedException("Карты не принадлежат текущему пользователю");
        }

        if (fromCard.getBalance() < amount) {
            throw new InsufficientFundsException("На карте ID=" + fromCardId + " недостаточно средств");
        }

        fromCard.setBalance(fromCard.getBalance() - amount);
        toCard.setBalance(toCard.getBalance() + amount);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Выполнен перевод: {} руб. с карты {} на карту {}", amount, fromCardId, toCardId);
    }

}