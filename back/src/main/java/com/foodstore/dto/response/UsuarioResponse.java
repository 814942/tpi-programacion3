package com.foodstore.dto.response;

import com.foodstore.model.enums.Rol;
import java.time.LocalDateTime;

public record UsuarioResponse(
    Long id,
    String nombre,
    String apellido,
    String email,
    String celular,
    Rol rol,
    LocalDateTime createdAt
) {}
