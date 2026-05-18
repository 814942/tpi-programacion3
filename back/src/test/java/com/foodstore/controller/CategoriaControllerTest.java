package com.foodstore.controller;

import com.foodstore.dto.request.CategoriaRequest;
import com.foodstore.dto.request.UpdateCategoriaRequest;
import com.foodstore.dto.response.CategoriaResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.service.CategoriaService;
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

import java.util.List;
import java.util.Map;

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
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    private final CategoriaResponse response = new CategoriaResponse(
            1L,
            "Hamburguesas",
            "Hamburguesas clásicas",
            "https://ejemplo.com/hamburguesa.jpg"
    );

    @Nested
    class FindAllTests {

        @Test
        @WithMockUser
        void shouldReturn200WithCategoryList() throws Exception {
            when(categoriaService.findAll()).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v1/categorias"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].nombre").value("Hamburguesas"))
                    .andExpect(jsonPath("$[0].descripcion").value("Hamburguesas clásicas"));
        }

        @Test
        @WithMockUser
        void shouldReturn200WithEmptyList() throws Exception {
            when(categoriaService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/categorias"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/categorias"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        @WithMockUser
        void shouldReturn200WhenCategoryExists() throws Exception {
            when(categoriaService.findById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/categorias/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Hamburguesas"));
        }

        @Test
        @WithMockUser
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            when(categoriaService.findById(999L))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            mockMvc.perform(get("/api/v1/categorias/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/categorias/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateTests {

        private final CategoriaRequest createRequest = new CategoriaRequest(
                "Hamburguesas",
                "Hamburguesas clásicas",
                "https://ejemplo.com/hamburguesa.jpg"
        );

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn201WhenCreated() throws Exception {
            when(categoriaService.create(any(CategoriaRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Hamburguesas"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenDuplicateName() throws Exception {
            when(categoriaService.create(any(CategoriaRequest.class)))
                    .thenThrow(new BusinessException("El nombre de categoría ya existe"));

            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenNombreBlank() throws Exception {
            Map<String, Object> body = Map.of(
                    "nombre", "",
                    "descripcion", "test",
                    "imagen", "https://ejemplo.com/img.jpg"
            );

            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenInvalidImageUrl() throws Exception {
            Map<String, Object> body = Map.of(
                    "nombre", "Hamburguesas",
                    "descripcion", "test",
                    "imagen", "xyz"
            );

            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateTests {

        private final UpdateCategoriaRequest updateRequest = new UpdateCategoriaRequest(
                "Actualizada",
                "Nueva descripción",
                "https://ejemplo.com/nueva.jpg"
        );

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WhenUpdated() throws Exception {
            CategoriaResponse updatedResponse = new CategoriaResponse(
                    1L,
                    "Actualizada",
                    "Nueva descripción",
                    "https://ejemplo.com/nueva.jpg"
            );

            when(categoriaService.update(eq(1L), any(UpdateCategoriaRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/v1/categorias/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Actualizada"))
                    .andExpect(jsonPath("$.descripcion").value("Nueva descripción"))
                    .andExpect(jsonPath("$.imagen").value("https://ejemplo.com/nueva.jpg"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            when(categoriaService.update(eq(999L), any(UpdateCategoriaRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            mockMvc.perform(put("/api/v1/categorias/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenDuplicateName() throws Exception {
            when(categoriaService.update(eq(1L), any(UpdateCategoriaRequest.class)))
                    .thenThrow(new BusinessException("El nombre de categoría ya existe"));

            mockMvc.perform(put("/api/v1/categorias/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(put("/api/v1/categorias/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/v1/categorias/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn204WhenDeleted() throws Exception {
            doNothing().when(categoriaService).deleteById(1L);

            mockMvc.perform(delete("/api/v1/categorias/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Categoria", "id", "999"))
                    .when(categoriaService).deleteById(999L);

            mockMvc.perform(delete("/api/v1/categorias/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/categorias/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/v1/categorias/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
