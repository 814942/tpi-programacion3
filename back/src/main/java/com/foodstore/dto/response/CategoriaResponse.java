package com.foodstore.dto.response;

public record CategoriaResponse(
    Long id,
    String nombre,
    String descripcion,
    String imagen
) {}
