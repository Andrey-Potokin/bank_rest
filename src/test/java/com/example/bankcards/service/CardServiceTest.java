package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.TransferNotAllowedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserCards() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        Card card = Card.builder()
                .id(100L)
                .number("1234567890123456")
                .owner("Test User")
                .expirationDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(1000.0)
                .user(user)
                .build();

        Page<Card> cardPage = new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true); // <-- ВАЖНО: добавляем это
        when(cardRepository.findByUserId(1L, PageRequest.of(0, 10))).thenReturn(cardPage);

        Page<CardDto> result = cardService.getUserCards(1L, PageRequest.of(0, 10));


        assertEquals(1, result.getTotalElements());
        assertEquals("**** **** **** 3456", result.getContent().get(0).getMaskedNumber());
        assertEquals("Test User", result.getContent().get(0).getOwner());
    }

    @Test
    void testCreateCard() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        CardDto dto = CardDto.builder()
                .owner("Иван Иванов")
                .maskedNumber("4111111111111111")
                .expirationDate("2025-12-01")
                .status(CardStatus.ACTIVE)
                .balance(1000.50)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Card savedCard = Card.builder()
                .id(200L)
                .owner(dto.getOwner())
                .number(dto.getMaskedNumber())
                .expirationDate(LocalDate.parse(dto.getExpirationDate()))
                .status(CardStatus.ACTIVE)
                .balance(dto.getBalance())
                .user(user)
                .build();

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardDto result = cardService.createCard(dto, 1L);

        assertEquals(200L, result.getId());
        assertEquals("Иван Иванов", result.getOwner());
        assertEquals("**** **** **** 1111", result.getMaskedNumber());
        assertEquals("2025-12-01", result.getExpirationDate());
        assertEquals(CardStatus.ACTIVE, result.getStatus()); // Сравниваем строки
        assertEquals(1000.50, result.getBalance());

        verify(cardRepository).save(argThat(card -> card.getUser().getId().equals(1L)));
    }

    @Test
    void testBlockCard_Success() {
        User user = User.builder().id(1L).username("user1").build();
        Card card = Card.builder().id(300L).user(user).status(CardStatus.ACTIVE).build();

        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getName()).thenReturn("user1");
        when(cardRepository.findById(300L)).thenReturn(Optional.of(card));


        cardService.blockCard(300L);

        verify(cardRepository).save(card);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
    }

    @Test
    void testBlockCard_AccessDenied() {
        User owner = User.builder().id(1L).username("owner").build();
        Card card = Card.builder().id(400L).user(owner).build();

        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getName()).thenReturn("otheruser");
        when(cardRepository.findById(400L)).thenReturn(Optional.of(card));


        assertThrows(AccessDeniedException.class, () -> cardService.blockCard(400L));
    }

    @Test
    void testTransfer_Success() {
        User user = User.builder().id(1L).username("user1").build();
        Card fromCard = Card.builder()
                .id(500L)
                .balance(1000.0)
                .user(user)
                .status(CardStatus.ACTIVE)
                .build();
        Card toCard = Card.builder()
                .id(600L)
                .balance(200.0)
                .user(user)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(500L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(600L)).thenReturn(Optional.of(toCard));


        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getName()).thenReturn("user1");


        cardService.transfer(500L, 600L, 300.0);


        assertEquals(700.0, fromCard.getBalance());
        assertEquals(500.0, toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void testTransfer_InsufficientFunds() {
        User user = User.builder().id(1L).username("user1").build();
        Card fromCard = Card.builder()
                .balance(100.0)
                .user(user)
                .status(CardStatus.ACTIVE)
                .build();
        Card toCard = Card.builder()
                .balance(200.0)
                .user(user)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(700L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(800L)).thenReturn(Optional.of(toCard));


        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getName()).thenReturn("user1");


        assertThrows(
                InsufficientFundsException.class,
                () -> cardService.transfer(700L, 800L, 200.0),
                "Баланс исходной карты недостаточен для перевода"
        );

        assertEquals(100.0, fromCard.getBalance());
        assertEquals(200.0, toCard.getBalance());
        verify(cardRepository, never()).save(fromCard);
    }

    @Test
    void testTransfer_DifferentUsers() {
        User user1 = User.builder().id(1L).username("user1").build();
        User user2 = User.builder().id(2L).username("user2").build();


        Card fromCard = Card.builder()
                .id(900L)
                .balance(1000.0)
                .user(user1)
                .status(CardStatus.ACTIVE)
                .build();
        Card toCard = Card.builder()
                .id(1000L)
                .balance(500.0)
                .user(user2)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(900L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(1000L)).thenReturn(Optional.of(toCard));


        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getName()).thenReturn("user1");

        assertThrows(
                TransferNotAllowedException.class,
                () -> cardService.transfer(900L, 1000L, 100.0),
                "Перевод между картами разных пользователей должен быть запрещён"
        );

        assertEquals(1000.0, fromCard.getBalance());
        assertEquals(500.0, toCard.getBalance());
        verify(cardRepository, never()).save(fromCard);
        verify(cardRepository, never()).save(toCard);
    }

    @Test
    void testGetUserCards_UserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> {
            cardService.getUserCards(999L, PageRequest.of(0, 10));
        });
    }

    @Test
    void testGetUserCards_InvalidUserId() {
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.getUserCards(-1L, PageRequest.of(0, 10));
        });
    }
}