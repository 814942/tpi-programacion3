package com.foodstore.controller;

import com.foodstore.dto.request.CreateUsuarioRequest;
import com.foodstore.dto.request.UpdateUsuarioRequest;
import com.foodstore.dto.response.PaginatedResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
            var paginated = PaginatedResponse.from(page);
            doReturn(paginated).when(usuarioService).findAll(any(Pageable.class), any());

            mockMvc.perform(get("/api/v1/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].nombre").value("Juan"))
                    .andExpect(jsonPath("$.content[0].email").value("juan@test.com"))
                    .andExpect(jsonPath("$.content[0].rol").value("USUARIO"))
                    .andExpect(jsonPath("$.content[0].password").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WithEmptyList() throws Exception {
            var emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            var paginated = PaginatedResponse.from(emptyPage);
            doReturn(paginated).when(usuarioService).findAll(any(Pageable.class), any());

            mockMvc.perform(get("/api/v1/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
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

        private final CreateUsuarioRequest createRequest = new CreateUsuarioRequest(
                "Juan", "Perez", "nuevo@test.com", "1234567890", "Password1!", Rol.USUARIO
        );

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn201WhenCreated() throws Exception {
                        when(usuarioService.create(any(CreateUsuarioRequest.class))).thenReturn(response);

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
                        when(usuarioService.create(any(CreateUsuarioRequest.class)))
                    .thenThrow(new BusinessException("El email ya está registrado"));

            mockMvc.perform(post("/api/v1/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenRequiredFieldMissing() throws Exception {
            Map<String, Object> body = Map.of(
                    "apellido", "Perez",
                    "email", "nuevo@test.com",
                    "password", "Password1!",
                    "rol", "USUARIO"
            );

            mockMvc.perform(post("/api/v1/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
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

        private final UpdateUsuarioRequest updateRequest = new UpdateUsuarioRequest(
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

            when(usuarioService.update(eq(1L), any(UpdateUsuarioRequest.class)))
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
            when(usuarioService.update(eq(999L), any(UpdateUsuarioRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Usuario", "id", "999"));

            mockMvc.perform(patch("/api/v1/usuarios/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenEmailDuplicate() throws Exception {
            when(usuarioService.update(eq(1L), any(UpdateUsuarioRequest.class)))
                    .thenThrow(new BusinessException("El email ya está registrado"));

            mockMvc.perform(patch("/api/v1/usuarios/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenUnknownFieldPresent() throws Exception {
            Map<String, Object> body = Map.of(
                    "nomber", "Tyron",
                    "email", "t_to_b@foodstore.com"
            );

            mockMvc.perform(patch("/api/v1/usuarios/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("bad_request"));
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
        void shouldReturn200WhenDeleted() throws Exception {
                        doNothing().when(usuarioService).deleteById(1L, 2L);

            mockMvc.perform(delete("/api/v1/usuarios/1")
                            .with(authentication(adminAuthentication(2L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Usuario eliminado correctamente"));
        }

        @Test
        void shouldReturn404WhenUserNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Usuario", "id", "999"))
                                        .when(usuarioService).deleteById(999L, 2L);

            mockMvc.perform(delete("/api/v1/usuarios/999")
                            .with(authentication(adminAuthentication(2L))))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403WhenAdminPrincipalIsInvalid() throws Exception {
            mockMvc.perform(delete("/api/v1/usuarios/1")
                            .with(authentication(adminAuthentication("admin-user"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/usuarios/1"))
                    .andExpect(status().isForbidden());
        }
    }

    private Authentication adminAuthentication(Object principal) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
