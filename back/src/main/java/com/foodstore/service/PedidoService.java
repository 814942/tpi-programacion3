package com.foodstore.service;

import com.foodstore.dto.request.PedidoRequest;
import com.foodstore.dto.response.DetallePedidoResponse;
import com.foodstore.dto.response.PedidoResponse;
import com.foodstore.dto.response.UsuarioResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.DetallePedido;
import com.foodstore.model.Pedido;
import com.foodstore.model.Producto;
import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Estado;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.PedidoRepository;
import com.foodstore.repository.ProductoRepository;
import com.foodstore.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final Map<Estado, Set<Estado>> TRANSICIONES_PERMITIDAS = Map.of(
            Estado.PENDIENTE, Set.of(Estado.CONFIRMADO, Estado.CANCELADO),
            Estado.CONFIRMADO, Set.of(Estado.TERMINADO, Estado.CANCELADO),
            Estado.TERMINADO, Set.of(),
            Estado.CANCELADO, Set.of()
    );

    @Transactional(readOnly = true)
    public List<PedidoResponse> findAll() {
        return pedidoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse findById(Long id, Long currentUserId) {
        Pedido pedido = pedidoRepository.findByIdOrThrow(id);

        boolean isOwner = pedido.getUsuario().getId().equals(currentUserId);
        if (!isOwner && !isAdmin(currentUserId)) {
            throw new AccessDeniedException("No tenés permisos para ver este pedido");
        }

        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> findByUsuario(Long currentUserId) {
        return pedidoRepository.findByUsuarioId(currentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PedidoResponse create(Long currentUserId, PedidoRequest request) {
        Usuario usuario = usuarioRepository.findByIdOrThrow(currentUserId);

        Pedido pedido = Pedido.builder()
                .fecha(LocalDateTime.now())
                .estado(Estado.PENDIENTE)
                .formaPago(request.formaPago())
                .total(BigDecimal.ZERO)
                .usuario(usuario)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (PedidoRequest.DetalleRequest detalleReq : request.detalles()) {
            Producto producto = productoRepository.findByIdForUpdateOrThrow(detalleReq.idProducto());

            if (!producto.getDisponible()) {
                throw new BusinessException("El producto '" + producto.getNombre() + "' no está disponible");
            }

            if (producto.getStock() < detalleReq.cantidad()) {
                throw new BusinessException(
                        "Stock insuficiente para '" + producto.getNombre() + "': disponible " +
                                producto.getStock() + ", solicitado " + detalleReq.cantidad());
            }

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(detalleReq.cantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            DetallePedido detalle = DetallePedido.builder()
                    .productoNombre(producto.getNombre())
                    .productoPrecio(producto.getPrecio())
                    .productoDescripcion(producto.getDescripcion())
                    .productoImagen(producto.getImagen())
                    .productoId(producto.getId())
                    .cantidad(detalleReq.cantidad())
                    .subtotal(subtotal)
                    .pedido(pedido)
                    .build();

            pedido.getDetalles().add(detalle);

            producto.setStock(producto.getStock() - detalleReq.cantidad());
            productoRepository.save(producto);

            total = total.add(subtotal);
        }

        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        pedidoRepository.save(pedido);
        log.info("Pedido {} creado por usuario {}", pedido.getId(), currentUserId);

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse updateEstado(Long id, Estado nuevoEstado) {
        Pedido pedido = pedidoRepository.findByIdOrThrow(id);

        Set<Estado> transicionesPermitidas = TRANSICIONES_PERMITIDAS.get(pedido.getEstado());
        if (transicionesPermitidas == null || !transicionesPermitidas.contains(nuevoEstado)) {
            throw new BusinessException(
                    "No se puede cambiar de " + pedido.getEstado() + " a " + nuevoEstado);
        }

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == Estado.CANCELADO) {
            restaurarStock(pedido);
        }

        pedidoRepository.save(pedido);
        log.info("Pedido {} cambiado a estado {}", id, nuevoEstado);

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Long id, Long currentUserId) {
        Pedido pedido = pedidoRepository.findByIdOrThrow(id);

        boolean isOwner = pedido.getUsuario().getId().equals(currentUserId);
        if (!isOwner && !isAdmin(currentUserId)) {
            throw new AccessDeniedException("No tenés permisos para cancelar este pedido");
        }

        if (pedido.getEstado() != Estado.PENDIENTE) {
            throw new BusinessException("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }

        restaurarStock(pedido);

        pedido.setEstado(Estado.CANCELADO);
        pedidoRepository.save(pedido);
        log.info("Pedido {} cancelado por usuario {}", id, currentUserId);

        return toResponse(pedido);
    }

    private void restaurarStock(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            try {
                if (detalle.getProductoId() != null) {
                    Producto producto = productoRepository.findByIdOrThrow(detalle.getProductoId());
                    producto.setStock(producto.getStock() + detalle.getCantidad());
                    productoRepository.save(producto);
                    log.debug("Stock restaurado: +{} para producto {} (pedido {})",
                            detalle.getCantidad(), producto.getNombre(), pedido.getId());
                }
            } catch (Exception e) {
                log.error("No se pudo restaurar stock del producto ID {} en pedido {}",
                        detalle.getProductoId(), pedido.getId(), e);
                throw new BusinessException(
                        "No se pudo restaurar el stock del producto ID " + detalle.getProductoId()
                                + " para el pedido " + pedido.getId());
            }
        }
    }

    private boolean isAdmin(Long userId) {
        return usuarioRepository.findByIdOrThrow(userId).getRol() == Rol.ADMIN;
    }

    private PedidoResponse toResponse(Pedido pedido) {
        Usuario usuario = pedido.getUsuario();
        UsuarioResponse usuarioResponse = new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getCelular(),
                usuario.getRol(),
                usuario.getCreatedAt()
        );

        List<DetallePedidoResponse> detallesResponse = pedido.getDetalles().stream()
                .map(d -> new DetallePedidoResponse(
                        d.getId(),
                        d.getProductoNombre(),
                        d.getProductoPrecio(),
                        d.getProductoDescripcion(),
                        d.getProductoImagen(),
                        d.getProductoId(),
                        d.getCantidad(),
                        d.getSubtotal()
                ))
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getFecha(),
                pedido.getEstado(),
                pedido.getFormaPago(),
                pedido.getTotal(),
                usuarioResponse,
                detallesResponse
        );
    }
}
