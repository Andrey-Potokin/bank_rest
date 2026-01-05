package com.example.bankcards.util;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public final class CardUtil {

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

    private static String maskCardNumber(String number) {
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