package com.foodstore.repository;

import com.foodstore.model.Producto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends BaseRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.eliminado = false")
    @Override
    List<Producto> findAll();

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.id = :id AND p.eliminado = false")
    @Override
    Optional<Producto> findById(Long id);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.categoria.id = :categoriaId AND p.eliminado = false")
    List<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.nombre = :nombre AND p.eliminado = false")
    List<Producto> findByNombreAndEliminadoFalse(@Param("nombre") String nombre);
}
