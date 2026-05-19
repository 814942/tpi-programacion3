package com.foodstore.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "detalles_pedido")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DetallePedido extends Base {

    @Column(name = "producto_nombre", nullable = false)
    private String productoNombre;

    @Column(name = "producto_precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal productoPrecio;

    @Column(name = "producto_descripcion")
    private String productoDescripcion;

    @Column(name = "producto_imagen")
    private String productoImagen;

    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
}
