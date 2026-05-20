package com.foodstore.repository;

import com.foodstore.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends BaseRepository<Pedido, Long> {

    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.eliminado = false ORDER BY p.fecha DESC")
    @Override
    List<Pedido> findAll();

    @Query(value = "SELECT DISTINCT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.eliminado = false ORDER BY p.fecha DESC",
           countQuery = "SELECT COUNT(p) FROM Pedido p WHERE p.eliminado = false")
    @Override
    Page<Pedido> findAll(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.eliminado = false AND (LOWER(CONCAT('', p.estado)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(CONCAT('', p.formaPago)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.usuario.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.usuario.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(p.fecha AS string) LIKE CONCAT('%', :search, '%')) ORDER BY p.fecha DESC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Pedido p LEFT JOIN p.usuario u WHERE p.eliminado = false AND (LOWER(CONCAT('', p.estado)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(CONCAT('', p.formaPago)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(p.fecha AS string) LIKE CONCAT('%', :search, '%'))")
    Page<Pedido> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.id = :id AND p.eliminado = false")
    @Override
    Optional<Pedido> findById(Long id);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.usuario.id = :usuarioId AND p.eliminado = false ORDER BY p.fecha DESC")
    List<Pedido> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query(value = "SELECT DISTINCT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.usuario.id = :usuarioId AND p.eliminado = false AND (LOWER(CONCAT('', p.estado)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(CONCAT('', p.formaPago)) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(p.fecha AS string) LIKE CONCAT('%', :search, '%')) ORDER BY p.fecha DESC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Pedido p WHERE p.usuario.id = :usuarioId AND p.eliminado = false AND (LOWER(CONCAT('', p.estado)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(CONCAT('', p.formaPago)) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(p.fecha AS string) LIKE CONCAT('%', :search, '%'))")
    Page<Pedido> searchByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("search") String search, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Pedido p JOIN FETCH p.usuario LEFT JOIN FETCH p.detalles WHERE p.usuario.id = :usuarioId AND p.eliminado = false ORDER BY p.fecha DESC",
           countQuery = "SELECT COUNT(p) FROM Pedido p WHERE p.usuario.id = :usuarioId AND p.eliminado = false")
    Page<Pedido> findByUsuarioIdPaginated(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
