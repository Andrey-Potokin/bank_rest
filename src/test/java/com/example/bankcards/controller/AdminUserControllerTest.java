package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser() throws Exception {
        UserDto requestDto = UserDto.builder().username("newadmin").build();
        UserDto responseDto = UserDto.builder().id(100L).username("newadmin").build();

        when(userService.createUser(any(UserDto.class), eq("password")))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/users?password=password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"newadmin\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUser() throws Exception {
        UserDto dto = UserDto.builder().id(200L).username("existinguser").build();

        when(userService.getUserById(200L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("existinguser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateRole() throws Exception {
        mockMvc.perform(put("/api/admin/users/300/role?role=ADMIN"))
                .andExpect(status().isOk());

        verify(userService).updateRole(300L, "ADMIN");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/400"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(400L);
    }
}