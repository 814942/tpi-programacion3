package com.foodstore.dto.request;

import com.foodstore.model.enums.Rol;

public record UsuarioRequest(
    String nombre,
    String apellido,
    String email,
    String celular,
    String password,
    Rol rol
) {}
