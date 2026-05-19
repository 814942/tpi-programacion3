package com.foodstore.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponse(
    Long id,
    String nombre,
    BigDecimal precio,
    String descripcion,
    Integer stock,
    String imagen,
    Boolean disponible,
    CategoriaResponse categoria,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
