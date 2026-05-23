package com.foodstore.dto.response;

public record AdminStatsResponse(
    long categorias,
    long productos,
    long pedidos,
    long usuarios
) {}
