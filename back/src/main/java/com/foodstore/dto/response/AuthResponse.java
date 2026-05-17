package com.foodstore.dto.response;

import com.foodstore.model.enums.Rol;

public record AuthResponse(
    String token,
    String type,
    Long id,
    String email,
    String nombre,
    String apellido,
    String celular,
    Rol role
) {
    public AuthResponse(String token, Long id, String email, String nombre, String apellido, String celular, Rol role) {
        this(token, "Bearer", id, email, nombre, apellido, celular, role);
    }
}
