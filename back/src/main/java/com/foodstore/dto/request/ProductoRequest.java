package com.foodstore.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", inclusive = false, message = "El precio debe ser mayor a 0.01")
    BigDecimal precio,

    @Size(max = 500, message = "La descripción debe tener hasta 500 caracteres")
    String descripcion,

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    Integer stock,

    @Pattern(regexp = "^(https?://.*)?$", message = "La imagen debe ser una URL válida (http/https)")
    String imagen,

    Boolean disponible,

    @NotNull(message = "La categoría es obligatoria")
    Long idCategoria
) {}
