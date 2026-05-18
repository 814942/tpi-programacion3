package com.foodstore.controller;

import com.foodstore.dto.request.UsuarioRequest;
import com.foodstore.dto.response.UsuarioResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.enums.Rol;
import com.foodstore.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    private final UsuarioResponse response = new UsuarioResponse(
            1L,
            "Juan",
            "Perez",
            "juan@test.com",
            "1234567890",
            Rol.USUARIO,
            LocalDateTime.of(2026, 5, 16, 12, 0)
    );

    @Nested
    class FindAllTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WithUserList() throws Exception {
            when(usuarioService.findAll()).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].nombre").value("Juan"))
                    .andExpect(jsonPath("$[0].email").value("juan@test.com"))
                    .andExpect(jsonPath("$[0].rol").value("USUARIO"))
                    .andExpect(jsonPath("$[0].password").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WithEmptyList() throws Exception {
            when(usuarioService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/usuarios"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WhenUserExists() throws Exception {
            when(usuarioService.findById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(usuarioService.findById(999L))
                    .thenThrow(new ResourceNotFoundException("Usuario", "id", "999"));

            mockMvc.perform(get("/api/v1/usuarios/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/usuarios/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class CreateTests {

        private final UsuarioRequest createRequest = new UsuarioRequest(
                "Juan", "Perez", "nuevo@test.com", "1234567890", "password123", Rol.USUARIO
        );

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn201WhenCreated() throws Exception {
            when(usuarioService.create(any(UsuarioRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenEmailDuplicate() throws Exception {
            when(usuarioService.create(any(UsuarioRequest.class)))
                    .thenThrow(new BusinessException("El email ya está registrado"));

            mockMvc.perform(post("/api/v1/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateTests {

        private final UsuarioRequest updateRequest = new UsuarioRequest(
                "Juan Actualizado", null, "nuevo@test.com", null, null, Rol.ADMIN
        );

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WhenUpdated() throws Exception {
            UsuarioResponse updatedResponse = new UsuarioResponse(
                    1L,
                    "Juan Actualizado",
                    "Perez",
                    "nuevo@test.com",
                    "1234567890",
                    Rol.ADMIN,
                    LocalDateTime.of(2026, 5, 16, 12, 0)
            );

            when(usuarioService.update(eq(1L), any(UsuarioRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(patch("/api/v1/usuarios/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Juan Actualizado"))
                    .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                    .andExpect(jsonPath("$.rol").value("ADMIN"))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(usuarioService.update(eq(999L), any(UsuarioRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Usuario", "id", "999"));

            mockMvc.perform(patch("/api/v1/usuarios/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenEmailDuplicate() throws Exception {
            when(usuarioService.update(eq(1L), any(UsuarioRequest.class)))
                    .thenThrow(new BusinessException("El email ya está registrado"));

            mockMvc.perform(patch("/api/v1/usuarios/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(patch("/api/v1/usuarios/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WhenDeleted() throws Exception {
            doNothing().when(usuarioService).deleteById(1L);

            mockMvc.perform(delete("/api/v1/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Usuario eliminado correctamente"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenUserNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Usuario", "id", "999"))
                    .when(usuarioService).deleteById(999L);

            mockMvc.perform(delete("/api/v1/usuarios/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/usuarios/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
