package com.foodstore.dto.request;

import com.foodstore.model.enums.FormaPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequest(
    @NotNull(message = "La forma de pago es obligatoria")
    FormaPago formaPago,

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    List<DetalleRequest> detalles
) {
    public record DetalleRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long idProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad
    ) {}
}
