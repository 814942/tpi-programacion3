package com.foodstore.controller;

import com.foodstore.dto.request.ProductoRequest;
import com.foodstore.dto.request.UpdateProductoRequest;
import com.foodstore.dto.response.CategoriaResponse;
import com.foodstore.dto.response.PaginatedResponse;
import com.foodstore.dto.response.ProductoResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.service.ProductoService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    private final CategoriaResponse categoriaResponse = new CategoriaResponse(
            1L,
            "Hamburguesas",
            "Hamburguesas clásicas",
            "https://ejemplo.com/hamburguesa.jpg"
    );

    private final ProductoResponse response = new ProductoResponse(
            1L,
            "Clásica",
            new BigDecimal("25000.00"),
            "Hamburguesa clásica",
            50,
            "https://ejemplo.com/clasica.jpg",
            true,
            categoriaResponse,
            null,
            null
    );

    @Nested
    class FindAllTests {

        @Test
        @WithMockUser
        void shouldReturn200WithProductList() throws Exception {
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
            var paginated = PaginatedResponse.from(page);
            doReturn(paginated).when(productoService).findAll(any(Pageable.class), any());

            mockMvc.perform(get("/api/v1/productos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].nombre").value("Clásica"))
                    .andExpect(jsonPath("$.content[0].precio").value(25000.00))
                    .andExpect(jsonPath("$.content[0].stock").value(50))
                    .andExpect(jsonPath("$.content[0].disponible").value(true))
                    .andExpect(jsonPath("$.content[0].categoria.id").value(1L))
                    .andExpect(jsonPath("$.content[0].categoria.nombre").value("Hamburguesas"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser
        void shouldReturn200WithEmptyList() throws Exception {
            var emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            var paginated = PaginatedResponse.from(emptyPage);
            doReturn(paginated).when(productoService).findAll(any(Pageable.class), any());

            mockMvc.perform(get("/api/v1/productos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/productos"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        @WithMockUser
        void shouldReturn200WhenProductExists() throws Exception {
            when(productoService.findById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/productos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Clásica"))
                    .andExpect(jsonPath("$.categoria.id").value(1L));
        }

        @Test
        @WithMockUser
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(productoService.findById(999L))
                    .thenThrow(new ResourceNotFoundException("Producto", "id", "999"));

            mockMvc.perform(get("/api/v1/productos/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/productos/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class FindByCategoriaIdTests {

        @Test
        @WithMockUser
        void shouldReturn200WhenCategoriaExists() throws Exception {
            PaginatedResponse<ProductoResponse> paginatedResponse = new PaginatedResponse<>(
                    List.of(response),
                    0,
                    20,
                    1,
                    1
            );
            when(productoService.findByCategoriaId(eq(1L), any(), isNull())).thenReturn(paginatedResponse);

            mockMvc.perform(get("/api/v1/productos/categoria/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].nombre").value("Clásica"));
        }

        @Test
        @WithMockUser
        void shouldReturn200WithEmptyListWhenCategoriaHasNoProducts() throws Exception {
            PaginatedResponse<ProductoResponse> paginatedResponse = new PaginatedResponse<>(
                    List.of(),
                    0,
                    20,
                    0,
                    0
            );
            when(productoService.findByCategoriaId(eq(1L), any(), isNull())).thenReturn(paginatedResponse);

            mockMvc.perform(get("/api/v1/productos/categoria/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser
        void shouldReturn404WhenCategoriaNotFound() throws Exception {
            when(productoService.findByCategoriaId(eq(999L), any(), isNull()))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            mockMvc.perform(get("/api/v1/productos/categoria/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/productos/categoria/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateTests {

        private final ProductoRequest createRequest = new ProductoRequest(
                "Clásica",
                new BigDecimal("25000.00"),
                "Hamburguesa clásica",
                50,
                "https://ejemplo.com/clasica.jpg",
                true,
                1L
        );

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn201WhenCreated() throws Exception {
            when(productoService.create(any(ProductoRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Clásica"))
                    .andExpect(jsonPath("$.categoria.id").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenNombreBlank() throws Exception {
            Map<String, Object> body = Map.of(
                    "nombre", "",
                    "precio", 25000.00,
                    "stock", 50,
                    "idCategoria", 1
            );

            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenNombreTooShort() throws Exception {
            Map<String, Object> body = Map.of(
                    "nombre", "A",
                    "precio", 25000.00,
                    "stock", 50,
                    "idCategoria", 1
            );

            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenPrecioNull() throws Exception {
            Map<String, Object> body = Map.of(
                    "nombre", "Clásica",
                    "stock", 50,
                    "idCategoria", 1
            );

            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenStockNull() throws Exception {
            Map<String, Object> body = Map.of(
                    "nombre", "Clásica",
                    "precio", 25000.00,
                    "idCategoria", 1
            );

            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenInvalidImageUrl() throws Exception {
            Map<String, Object> body = Map.of(
                    "nombre", "Clásica",
                    "precio", 25000.00,
                    "stock", 50,
                    "imagen", "xyz",
                    "idCategoria", 1
            );

            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenCategoriaMissing() throws Exception {
            when(productoService.create(any(ProductoRequest.class)))
                    .thenThrow(new BusinessException("La categoría especificada no existe"));

            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/productos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateTests {

        private final UpdateProductoRequest updateRequest = new UpdateProductoRequest(
                "Actualizada",
                new BigDecimal("30000.00"),
                "Nueva descripción",
                20,
                "https://ejemplo.com/nueva.jpg",
                false,
                null
        );

        private final ProductoResponse updatedResponse = new ProductoResponse(
                1L,
                "Actualizada",
                new BigDecimal("30000.00"),
                "Nueva descripción",
                20,
                "https://ejemplo.com/nueva.jpg",
                false,
                categoriaResponse,
                null,
                null
        );

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WhenUpdated() throws Exception {
            when(productoService.update(eq(1L), any(UpdateProductoRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/v1/productos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Actualizada"))
                    .andExpect(jsonPath("$.precio").value(30000.00))
                    .andExpect(jsonPath("$.stock").value(20))
                    .andExpect(jsonPath("$.disponible").value(false));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(productoService.update(eq(999L), any(UpdateProductoRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Producto", "id", "999"));

            mockMvc.perform(put("/api/v1/productos/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(put("/api/v1/productos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/v1/productos/1")
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
            doNothing().when(productoService).deleteById(1L);

            mockMvc.perform(delete("/api/v1/productos/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenProductNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Producto", "id", "999"))
                    .when(productoService).deleteById(999L);

            mockMvc.perform(delete("/api/v1/productos/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/productos/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/v1/productos/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
