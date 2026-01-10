package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser() throws Exception {
        UserCreateRequest requestDto = UserCreateRequest.builder().username("newuser").build();
        UserResponse responseDto = UserResponse.builder().id(100L).username("newuser").build();

        when(userService.createUser(any(UserCreateRequest.class), eq("password")))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/users?password=password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"newuser\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUser() throws Exception {
        UserResponse dto = UserResponse.builder().id(200L).username("existinguser").build();

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

        verify(userService).updateRole(300L, UserRole.ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/400"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(400L);
    }

}