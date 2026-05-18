package com.foodstore.controller;

import com.foodstore.dto.request.LoginRequest;
import com.foodstore.dto.request.RegisterRequest;
import com.foodstore.dto.response.AuthResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.enums.Rol;
import com.foodstore.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private final AuthResponse authResponse = new AuthResponse(
            "jwt-token", 1L, "juan@test.com",
            "Juan", "Perez", "1234567890", Rol.USUARIO
    );

    @Nested
    class LoginTests {

        @Test
        void shouldReturn200WithTokenWhenCredentialsAreValid() throws Exception {
            LoginRequest request = new LoginRequest("juan@test.com", "Pass1234!");
            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("juan@test.com"))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.apellido").value("Perez"))
                    .andExpect(jsonPath("$.celular").value("1234567890"))
                    .andExpect(jsonPath("$.role").value("USUARIO"));
        }

        @Test
        void shouldReturn400WhenEmailDoesNotExist() throws Exception {
            LoginRequest request = new LoginRequest("inexistente@test.com", "Pass1234!");
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException("Email o contraseña inválidos"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"))
                    .andExpect(jsonPath("$.message").value("Email o contraseña inválidos"));
        }

        @Test
        void shouldReturn400WhenPasswordIsIncorrect() throws Exception {
            LoginRequest request = new LoginRequest("juan@test.com", "WrongPass1!");
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException("Email o contraseña inválidos"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"))
                    .andExpect(jsonPath("$.message").value("Email o contraseña inválidos"));
        }

        @Test
        void shouldReturn400WhenEmailIsEmpty() throws Exception {
            LoginRequest request = new LoginRequest("", "Pass1234!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        void shouldReturn400WhenPasswordIsEmpty() throws Exception {
            LoginRequest request = new LoginRequest("juan@test.com", "");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            LoginRequest request = new LoginRequest("email-invalido", "Pass1234!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }
    }

    @Nested
    class RegisterTests {

        @Test
        void shouldReturn201WithTokenWhenDataIsValid() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Juan", "Perez", "nuevo@test.com", "1234567890", "Pass1234!"
            );
            when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("juan@test.com"))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.apellido").value("Perez"))
                    .andExpect(jsonPath("$.celular").value("1234567890"))
                    .andExpect(jsonPath("$.role").value("USUARIO"));
        }

        @Test
        void shouldReturn400WhenEmailAlreadyExists() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Juan", "Perez", "existente@test.com", null, "Pass1234!"
            );
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new BusinessException("El email ya está registrado"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"))
                    .andExpect(jsonPath("$.message").value("El email ya está registrado"));
        }

        @Test
        void shouldReturn400WhenNombreIsEmpty() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "", "Perez", "nuevo@test.com", "1234567890", "Pass1234!"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        void shouldReturn400WhenApellidoIsEmpty() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Juan", "", "nuevo@test.com", null, "Pass1234!"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        void shouldReturn400WhenPasswordIsLessThan8Chars() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Juan", "Perez", "nuevo@test.com", null, "12345"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Juan", "Perez", "email-invalido", null, "Pass1234!"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        void shouldReturn201WhenCelularIsNull() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Juan", "Perez", "nuevo@test.com", null, "Pass1234!"
            );
            when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }
}
