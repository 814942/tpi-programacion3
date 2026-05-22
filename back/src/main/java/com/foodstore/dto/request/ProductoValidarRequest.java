package com.foodstore.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ProductoValidarRequest(
    @NotEmpty(message = "La lista de IDs no puede estar vacía")
    List<Long> ids
) {}
