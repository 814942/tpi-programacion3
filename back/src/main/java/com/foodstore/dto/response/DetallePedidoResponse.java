package com.foodstore.dto.response;

import java.math.BigDecimal;

public record DetallePedidoResponse(
    Long id,
    String productoNombre,
    BigDecimal productoPrecio,
    String productoDescripcion,
    String productoImagen,
    Long productoId,
    Integer cantidad,
    BigDecimal subtotal
) {}
