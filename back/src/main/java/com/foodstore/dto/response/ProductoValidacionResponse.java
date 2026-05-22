package com.foodstore.dto.response;

public record ProductoValidacionResponse(
    Long id,
    boolean existe,
    boolean disponible,
    int stock
) {}
