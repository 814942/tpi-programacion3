package com.foodstore.repository;

import com.foodstore.model.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends BaseRepository<Pedido, Long> {

    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.eliminado = false ORDER BY p.fecha DESC")
    @Override
    List<Pedido> findAll();

    @Query("SELECT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.id = :id AND p.eliminado = false")
    @Override
    Optional<Pedido> findById(Long id);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.usuario.id = :usuarioId AND p.eliminado = false ORDER BY p.fecha DESC")
    List<Pedido> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
