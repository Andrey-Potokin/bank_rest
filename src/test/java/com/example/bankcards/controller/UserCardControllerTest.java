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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc(addFilters = false)
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    @WithMockUser(roles = "USER")
    void testGetUserCards() throws Exception {
        CardDto dto = CardDto.builder().id(1L).owner("User Test").build();
        Page<CardDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(cardService.getUserCards(any(Long.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/user/cards")
                        .param("userId", "123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].owner").value("User Test"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testBlockCard() throws Exception {
        mockMvc.perform(post("/api/user/cards/5/block"))
                .andExpect(status().isOk());

        verify(cardService).blockCard(5L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testTransfer() throws Exception {
        mockMvc.perform(post("/api/user/cards/transfer")
                        .param("fromCardId", "10")
                        .param("toCardId", "20")
                        .param("amount", "100.0"))
                .andExpect(status().isOk());

        verify(cardService).transfer(10L, 20L, 100.0);
    }

}