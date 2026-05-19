package com.foodstore.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCategoriaRequest(
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @Size(max = 500, message = "La descripción debe tener hasta 500 caracteres")
    String descripcion,

    @Pattern(regexp = "^(https?://).+", message = "La imagen debe ser una URL válida (http/https)")
    String imagen
) {}
