package com.example.bankcards.util;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;

import java.time.LocalDate;

public class CardUtil {

    public static CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedNumber(maskCardNumber(card.getNumber()));
        dto.setOwner(card.getOwner());
        dto.setExpirationDate(card.getExpirationDate().toString());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    public static Card toEntity(CardDto dto) {
        Card card = new Card();
        card.setId(dto.getId());
        card.setOwner(dto.getOwner());
        card.setExpirationDate(LocalDate.parse(dto.getExpirationDate()));
        card.setStatus(dto.getStatus());
        card.setBalance(dto.getBalance());
        return card;
    }

    private static String maskCardNumber(String number) {
        if (number == null || number.length() < 4) {
            return number;
        }

        String lastFour = number.substring(number.length() - 4);
        StringBuilder masked = new StringBuilder();

        for (int i = 0; i < number.length() - 4; i++) {
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