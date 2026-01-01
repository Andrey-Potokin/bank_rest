package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllCards() throws Exception {
        CardDto dto = CardDto.builder().id(1L).owner("Admin Test").build();
        Page<CardDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);


        when(cardService.getAllCards(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].owner").value("Admin Test"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCard() throws Exception {
        CardDto requestDto = CardDto.builder().owner("New Card").build();
        CardDto responseDto = CardDto.builder().id(2L).owner("New Card").build();

        when(cardService.createCard(any(CardDto.class), eq(10L))).thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards?userId=10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"owner\": \"New Card\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateCard() throws Exception {
        mockMvc.perform(put("/api/admin/cards/3/activate"))
                .andExpect(status().isOk());

        verify(cardService).activateCard(3L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteCard() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/4"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(4L);
    }
}