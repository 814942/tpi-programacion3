package com.foodstore.dto.response;

import com.foodstore.model.enums.Estado;
import com.foodstore.model.enums.FormaPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    LocalDateTime fecha,
    Estado estado,
    FormaPago formaPago,
    BigDecimal total,
    UsuarioResponse usuario,
    List<DetallePedidoResponse> detalles
) {}
