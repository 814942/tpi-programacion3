package com.foodstore.dto.request;

import com.foodstore.model.enums.Estado;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(
    @NotNull(message = "El estado es obligatorio")
    Estado estado
) {}
