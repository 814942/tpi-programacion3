package com.foodstore.controller;

import com.foodstore.config.WithAuthenticatedUser;
import com.foodstore.dto.request.EstadoRequest;
import com.foodstore.dto.request.PedidoRequest;
import com.foodstore.dto.response.DetallePedidoResponse;
import com.foodstore.dto.response.PaginatedResponse;
import com.foodstore.dto.response.PedidoResponse;
import com.foodstore.dto.response.UsuarioResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.enums.Estado;
import com.foodstore.model.enums.FormaPago;
import com.foodstore.model.enums.Rol;
import com.foodstore.service.PedidoService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    private final UsuarioResponse usuarioResponse = new UsuarioResponse(
            1L, "Juan", "Pérez", "juan@example.com", "123456789", Rol.USUARIO, LocalDateTime.now()
    );

    private final List<DetallePedidoResponse> detallesResponse = List.of(
            new DetallePedidoResponse(100L, "Clásica", new BigDecimal("25000.00"),
                    "Hamburguesa clásica", "https://ejemplo.com/clasica.jpg", 1L,
                    2, new BigDecimal("50000.00"))
    );

    private final PedidoResponse pedidoResponse = new PedidoResponse(
            1L, LocalDateTime.now(), Estado.PENDIENTE, FormaPago.TARJETA,
            new BigDecimal("50000.00"), usuarioResponse, detallesResponse
    );

    @Nested
    class CreateTests {

        private final PedidoRequest createRequest = new PedidoRequest(
                FormaPago.TARJETA,
                List.of(new PedidoRequest.DetalleRequest(10L, 2))
        );

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn201WhenCreated() throws Exception {
            when(pedidoService.create(eq(1L), any(PedidoRequest.class))).thenReturn(pedidoResponse);

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                    .andExpect(jsonPath("$.formaPago").value("TARJETA"))
                    .andExpect(jsonPath("$.total").value(50000.00))
                    .andExpect(jsonPath("$.usuario.id").value(1))
                    .andExpect(jsonPath("$.detalles[0].productoNombre").value("Clásica"));
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn404WhenProductoNotFound() throws Exception {
            when(pedidoService.create(eq(1L), any(PedidoRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Producto", "id", "10"));

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn400WhenBusinessError() throws Exception {
            when(pedidoService.create(eq(1L), any(PedidoRequest.class)))
                    .thenThrow(new BusinessException("Stock insuficiente"));

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn400WhenFormaPagoNull() throws Exception {
            Map<String, Object> body = Map.of(
                    "detalles", List.of(Map.of("idProducto", 10, "cantidad", 2))
            );

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn400WhenDetallesEmpty() throws Exception {
            Map<String, Object> body = Map.of(
                    "formaPago", "TARJETA",
                    "detalles", List.of()
            );

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn400WhenCantidadIsZero() throws Exception {
            Map<String, Object> body = Map.of(
                    "formaPago", "TARJETA",
                    "detalles", List.of(Map.of("idProducto", 10, "cantidad", 0))
            );

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("validation_error"));
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class FindByUsuarioTests {

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn200WithPedidosList() throws Exception {
            when(pedidoService.findByUsuario(1L)).thenReturn(List.of(pedidoResponse));

            mockMvc.perform(get("/api/v1/pedidos/usuario"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                    .andExpect(jsonPath("$[0].usuario.id").value(1));
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn200WithEmptyList() throws Exception {
            when(pedidoService.findByUsuario(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/pedidos/usuario"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/pedidos/usuario"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class FindAllTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WithPedidosList() throws Exception {
            var page = new PageImpl<>(List.of(pedidoResponse), PageRequest.of(0, 20), 1);
            var paginated = PaginatedResponse.from(page);
            doReturn(paginated).when(pedidoService).findAll(any(Pageable.class), any());

            mockMvc.perform(get("/api/v1/pedidos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"))
                    .andExpect(jsonPath("$.page").value(0));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WithEmptyList() throws Exception {
            var emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            var paginated = PaginatedResponse.from(emptyPage);
            doReturn(paginated).when(pedidoService).findAll(any(Pageable.class), any());

            mockMvc.perform(get("/api/v1/pedidos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/pedidos"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/pedidos"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn200WhenOwner() throws Exception {
            when(pedidoService.findById(1L, 1L)).thenReturn(pedidoResponse);

            mockMvc.perform(get("/api/v1/pedidos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        }

        @Test
        @WithAuthenticatedUser(userId = 2L, role = "ADMIN")
        void shouldReturn200WhenAdmin() throws Exception {
            when(pedidoService.findById(1L, 2L)).thenReturn(pedidoResponse);

            mockMvc.perform(get("/api/v1/pedidos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithAuthenticatedUser(userId = 3L)
        void shouldReturn403WhenNotOwnerAndNotAdmin() throws Exception {
            when(pedidoService.findById(1L, 3L))
                    .thenThrow(new org.springframework.security.access.AccessDeniedException("No tenés permisos"));

            mockMvc.perform(get("/api/v1/pedidos/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn404WhenNotFound() throws Exception {
            when(pedidoService.findById(999L, 1L))
                    .thenThrow(new ResourceNotFoundException("Pedido", "id", "999"));

            mockMvc.perform(get("/api/v1/pedidos/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/pedidos/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateEstadoTests {

        private final EstadoRequest estadoRequest = new EstadoRequest(Estado.CONFIRMADO);

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200WhenEstadoUpdated() throws Exception {
            PedidoResponse updated = new PedidoResponse(
                    1L, LocalDateTime.now(), Estado.CONFIRMADO, FormaPago.TARJETA,
                    new BigDecimal("50000.00"), usuarioResponse, detallesResponse
            );
            when(pedidoService.updateEstado(1L, Estado.CONFIRMADO)).thenReturn(updated);

            mockMvc.perform(patch("/api/v1/pedidos/1/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(estadoRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenInvalidTransition() throws Exception {
            when(pedidoService.updateEstado(1L, Estado.CONFIRMADO))
                    .thenThrow(new BusinessException("No se puede cambiar de TERMINADO a CONFIRMADO"));

            mockMvc.perform(patch("/api/v1/pedidos/1/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(estadoRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenPedidoNotFound() throws Exception {
            when(pedidoService.updateEstado(999L, Estado.CONFIRMADO))
                    .thenThrow(new ResourceNotFoundException("Pedido", "id", "999"));

            mockMvc.perform(patch("/api/v1/pedidos/999/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(estadoRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USUARIO")
        void shouldReturn403WhenNotAdmin() throws Exception {
            mockMvc.perform(patch("/api/v1/pedidos/1/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(estadoRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(patch("/api/v1/pedidos/1/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(estadoRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CancelarTests {

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn200WhenOwnerCancels() throws Exception {
            PedidoResponse cancelled = new PedidoResponse(
                    1L, LocalDateTime.now(), Estado.CANCELADO, FormaPago.TARJETA,
                    new BigDecimal("50000.00"), usuarioResponse, detallesResponse
            );
            when(pedidoService.cancelar(1L, 1L)).thenReturn(cancelled);

            mockMvc.perform(patch("/api/v1/pedidos/1/cancelar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CANCELADO"));
        }

        @Test
        @WithAuthenticatedUser(userId = 2L, role = "ADMIN")
        void shouldReturn200WhenAdminCancels() throws Exception {
            PedidoResponse cancelled = new PedidoResponse(
                    1L, LocalDateTime.now(), Estado.CANCELADO, FormaPago.TARJETA,
                    new BigDecimal("50000.00"), usuarioResponse, detallesResponse
            );
            when(pedidoService.cancelar(1L, 2L)).thenReturn(cancelled);

            mockMvc.perform(patch("/api/v1/pedidos/1/cancelar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CANCELADO"));
        }

        @Test
        @WithAuthenticatedUser(userId = 3L)
        void shouldReturn403WhenNotOwnerAndNotAdmin() throws Exception {
            when(pedidoService.cancelar(1L, 3L))
                    .thenThrow(new org.springframework.security.access.AccessDeniedException("No tenés permisos"));

            mockMvc.perform(patch("/api/v1/pedidos/1/cancelar"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn400WhenNotPendiente() throws Exception {
            when(pedidoService.cancelar(1L, 1L))
                    .thenThrow(new BusinessException("Solo se pueden cancelar pedidos en estado PENDIENTE"));

            mockMvc.perform(patch("/api/v1/pedidos/1/cancelar"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("business_error"));
        }

        @Test
        @WithAuthenticatedUser(userId = 1L)
        void shouldReturn404WhenPedidoNotFound() throws Exception {
            when(pedidoService.cancelar(999L, 1L))
                    .thenThrow(new ResourceNotFoundException("Pedido", "id", "999"));

            mockMvc.perform(patch("/api/v1/pedidos/999/cancelar"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(patch("/api/v1/pedidos/1/cancelar"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
