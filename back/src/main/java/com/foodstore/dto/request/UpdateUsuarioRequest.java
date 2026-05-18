package com.foodstore.dto.request;

import com.foodstore.model.enums.Rol;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUsuarioRequest(
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String nombre,

    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    String apellido,

    @Email(message = "Formato de email inválido")
    String email,

    @Pattern(regexp = "\\d*", message = "El celular debe contener solo números")
    @Size(max = 20, message = "El celular debe tener hasta 20 caracteres")
    String celular,

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%&!]).+$",
        message = "La contraseña debe tener mayúscula, minúscula, número y símbolo (@#$%&!)"
    )
    String password,

    Rol rol
) {
    @AssertTrue(message = "Debe enviar al menos un campo para actualizar")
    public boolean hasAnyFieldToUpdate() {
        return nombre != null
                || apellido != null
                || email != null
                || celular != null
                || password != null
                || rol != null;
    }
}