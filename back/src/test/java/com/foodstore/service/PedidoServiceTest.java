package com.foodstore.service;

import com.foodstore.dto.request.PedidoRequest;
import com.foodstore.dto.response.PedidoResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.DetallePedido;
import com.foodstore.model.Pedido;
import com.foodstore.model.Producto;
import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Estado;
import com.foodstore.model.enums.FormaPago;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.PedidoRepository;
import com.foodstore.repository.ProductoRepository;
import com.foodstore.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private Usuario admin;
    private Producto producto;
    private Pedido pedido;
    private PedidoRequest.DetalleRequest detalleRequest;
    private PedidoRequest pedidoRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@example.com")
                .celular("123456789")
                .rol(Rol.USUARIO)
                .build();

        admin = Usuario.builder()
                .id(2L)
                .nombre("Admin")
                .apellido("Root")
                .email("admin@example.com")
                .rol(Rol.ADMIN)
                .build();

        producto = Producto.builder()
                .id(10L)
                .nombre("Clásica")
                .precio(new BigDecimal("25000.00"))
                .descripcion("Hamburguesa clásica")
                .imagen("https://ejemplo.com/clasica.jpg")
                .stock(50)
                .disponible(true)
                .build();

        DetallePedido detalle = DetallePedido.builder()
                .id(100L)
                .productoNombre("Clásica")
                .productoPrecio(new BigDecimal("25000.00"))
                .productoDescripcion("Hamburguesa clásica")
                .productoImagen("https://ejemplo.com/clasica.jpg")
                .productoId(10L)
                .cantidad(2)
                .subtotal(new BigDecimal("50000.00"))
                .build();

        pedido = Pedido.builder()
                .id(1L)
                .fecha(LocalDateTime.now())
                .estado(Estado.PENDIENTE)
                .formaPago(FormaPago.TARJETA)
                .total(new BigDecimal("50000.00"))
                .usuario(usuario)
                .detalles(List.of(detalle))
                .build();

        detalleRequest = new PedidoRequest.DetalleRequest(10L, 2);
        pedidoRequest = new PedidoRequest(FormaPago.TARJETA, List.of(detalleRequest));
    }

    @Nested
    class CreateTests {

        @Test
        void shouldCreatePedidoSuccessfully() {
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(productoRepository.findByIdOrThrow(10L)).thenReturn(producto);
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            PedidoResponse result = pedidoService.create(1L, pedidoRequest);

            assertThat(result).isNotNull();
            assertThat(result.total()).isEqualByComparingTo(new BigDecimal("50000.00"));
            assertThat(result.estado()).isEqualTo(Estado.PENDIENTE);
            assertThat(result.formaPago()).isEqualTo(FormaPago.TARJETA);
            assertThat(result.detalles()).hasSize(1);
            assertThat(result.detalles().get(0).productoNombre()).isEqualTo("Clásica");
            assertThat(result.detalles().get(0).subtotal()).isEqualByComparingTo(new BigDecimal("50000.00"));

            ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
            verify(pedidoRepository).save(pedidoCaptor.capture());
            Pedido saved = pedidoCaptor.getValue();
            assertThat(saved.getFecha()).isNotNull();
            assertThat(saved.getEstado()).isEqualTo(Estado.PENDIENTE);
            assertThat(saved.getFormaPago()).isEqualTo(FormaPago.TARJETA);
            assertThat(saved.getTotal()).isEqualByComparingTo(new BigDecimal("50000.00"));
            assertThat(saved.getUsuario().getId()).isEqualTo(1L);
            assertThat(saved.getDetalles()).hasSize(1);

            DetallePedido savedDetalle = saved.getDetalles().get(0);
            assertThat(savedDetalle.getProductoNombre()).isEqualTo("Clásica");
            assertThat(savedDetalle.getProductoPrecio()).isEqualByComparingTo(new BigDecimal("25000.00"));
            assertThat(savedDetalle.getCantidad()).isEqualTo(2);
            assertThat(savedDetalle.getSubtotal()).isEqualByComparingTo(new BigDecimal("50000.00"));

            verify(productoRepository).save(producto);
            assertThat(producto.getStock()).isEqualTo(48);
        }

        @Test
        void shouldThrowWhenUsuarioNotFound() {
            when(usuarioRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Usuario", "id", "999"));

            assertThatThrownBy(() -> pedidoService.create(999L, pedidoRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario");

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenProductoNotFound() {
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(productoRepository.findByIdOrThrow(10L))
                    .thenThrow(new ResourceNotFoundException("Producto", "id", "10"));

            assertThatThrownBy(() -> pedidoService.create(1L, pedidoRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Producto");

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenProductoNotDisponible() {
            producto.setDisponible(false);
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(productoRepository.findByIdOrThrow(10L)).thenReturn(producto);

            assertThatThrownBy(() -> pedidoService.create(1L, pedidoRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no está disponible");

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenStockInsuficiente() {
            producto.setStock(1);
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(productoRepository.findByIdOrThrow(10L)).thenReturn(producto);

            assertThatThrownBy(() -> pedidoService.create(1L, pedidoRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Stock insuficiente");

            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldReturnPedidoWhenOwner() {
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);

            PedidoResponse result = pedidoService.findById(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        void shouldThrowWhenNotOwnerAndNotAdmin() {
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(usuarioRepository.findByIdOrThrow(3L)).thenReturn(
                    Usuario.builder().id(3L).rol(Rol.USUARIO).build()
            );

            assertThatThrownBy(() -> pedidoService.findById(1L, 3L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("No tenés permisos");
        }

        @Test
        void shouldReturnPedidoWhenAdmin() {
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(usuarioRepository.findByIdOrThrow(2L)).thenReturn(admin);

            PedidoResponse result = pedidoService.findById(1L, 2L);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
        }
    }

    @Nested
    class FindByUsuarioTests {

        @Test
        void shouldReturnPedidosForUsuario() {
            when(pedidoRepository.findByUsuarioId(1L)).thenReturn(List.of(pedido));

            List<PedidoResponse> result = pedidoService.findByUsuario(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }

        @Test
        void shouldReturnEmptyListWhenNoPedidos() {
            when(pedidoRepository.findByUsuarioId(1L)).thenReturn(List.of());

            List<PedidoResponse> result = pedidoService.findByUsuario(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnAllPedidos() {
            when(pedidoRepository.findAll()).thenReturn(List.of(pedido));

            List<PedidoResponse> result = pedidoService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }
    }

    @Nested
    class UpdateEstadoTests {

        @Test
        void shouldTransitionFromPendienteToConfirmado() {
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            PedidoResponse result = pedidoService.updateEstado(1L, Estado.CONFIRMADO);

            assertThat(result.estado()).isEqualTo(Estado.CONFIRMADO);
            ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
            verify(pedidoRepository).save(captor.capture());
            assertThat(captor.getValue().getEstado()).isEqualTo(Estado.CONFIRMADO);
        }

        @Test
        void shouldTransitionFromPendienteToCancelado() {
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            PedidoResponse result = pedidoService.updateEstado(1L, Estado.CANCELADO);

            assertThat(result.estado()).isEqualTo(Estado.CANCELADO);
        }

        @Test
        void shouldTransitionFromConfirmadoToTerminado() {
            pedido.setEstado(Estado.CONFIRMADO);
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            PedidoResponse result = pedidoService.updateEstado(1L, Estado.TERMINADO);

            assertThat(result.estado()).isEqualTo(Estado.TERMINADO);
        }

        @Test
        void shouldThrowWhenTransitionNotAllowed() {
            pedido.setEstado(Estado.TERMINADO);
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);

            assertThatThrownBy(() -> pedidoService.updateEstado(1L, Estado.CONFIRMADO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No se puede cambiar");
        }

        @Test
        void shouldThrowWhenCanceladoToAny() {
            pedido.setEstado(Estado.CANCELADO);
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);

            assertThatThrownBy(() -> pedidoService.updateEstado(1L, Estado.PENDIENTE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No se puede cambiar");
        }
    }

    @Nested
    class CancelarTests {

        @Test
        void shouldCancelarPedidoAndRestoreStock_WhenOwner() {
            Producto restoredProduct = Producto.builder()
                    .id(10L)
                    .nombre("Clásica")
                    .precio(new BigDecimal("25000.00"))
                    .stock(48)
                    .disponible(true)
                    .build();
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(productoRepository.findByIdOrThrow(10L)).thenReturn(restoredProduct);
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            PedidoResponse result = pedidoService.cancelar(1L, 1L);

            assertThat(result.estado()).isEqualTo(Estado.CANCELADO);
            assertThat(restoredProduct.getStock()).isEqualTo(50);
            verify(productoRepository).save(restoredProduct);
            ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
            verify(pedidoRepository).save(captor.capture());
            assertThat(captor.getValue().getEstado()).isEqualTo(Estado.CANCELADO);
        }

        @Test
        void shouldCancelarPedidoAndRestoreStock_WhenAdmin() {
            Producto restoredProduct = Producto.builder()
                    .id(10L)
                    .nombre("Clásica")
                    .precio(new BigDecimal("25000.00"))
                    .stock(48)
                    .disponible(true)
                    .build();
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(usuarioRepository.findByIdOrThrow(2L)).thenReturn(admin);
            when(productoRepository.findByIdOrThrow(10L)).thenReturn(restoredProduct);
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

            PedidoResponse result = pedidoService.cancelar(1L, 2L);

            assertThat(result.estado()).isEqualTo(Estado.CANCELADO);
            assertThat(restoredProduct.getStock()).isEqualTo(50);
        }

        @Test
        void shouldThrowWhenNotOwnerAndNotAdmin() {
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);
            when(usuarioRepository.findByIdOrThrow(3L)).thenReturn(
                    Usuario.builder().id(3L).rol(Rol.USUARIO).build()
            );

            assertThatThrownBy(() -> pedidoService.cancelar(1L, 3L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("No tenés permisos");
        }

        @Test
        void shouldThrowWhenNotPendiente() {
            pedido.setEstado(Estado.CONFIRMADO);
            when(pedidoRepository.findByIdOrThrow(1L)).thenReturn(pedido);

            assertThatThrownBy(() -> pedidoService.cancelar(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }
    }
}
