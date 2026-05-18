package com.foodstore.dto.request;

import com.foodstore.model.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUsuarioRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    String apellido,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    String email,

    @Pattern(regexp = "\\d*", message = "El celular debe contener solo números")
    @Size(max = 20, message = "El celular debe tener hasta 20 caracteres")
    String celular,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%&!]).+$",
        message = "La contraseña debe tener mayúscula, minúscula, número y símbolo (@#$%&!)"
    )
    String password,

    @NotNull(message = "El rol es obligatorio")
    Rol rol
) {}