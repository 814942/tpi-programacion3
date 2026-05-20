package com.foodstore.repository;

import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends BaseRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.eliminado = false")
    @Override
    List<Producto> findAll();

    @Query(value = "SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.eliminado = false",
           countQuery = "SELECT COUNT(p) FROM Producto p WHERE p.eliminado = false")
    @Override
    Page<Producto> findAll(Pageable pageable);

    @Query(value = "SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.eliminado = false AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(p) FROM Producto p WHERE p.eliminado = false AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Producto> searchByNombre(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.id = :id AND p.eliminado = false")
    @Override
    Optional<Producto> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.id = :id AND p.eliminado = false")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    default Producto findByIdForUpdateOrThrow(Long id) {
        return findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id.toString()));
    }

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.categoria.id = :categoriaId AND p.eliminado = false")
    List<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query(value = "SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.categoria.id = :categoriaId AND p.eliminado = false AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.eliminado = false AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Producto> searchByCategoriaId(@Param("categoriaId") Long categoriaId, @Param("search") String search, Pageable pageable);

    @Query(value = "SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.categoria.id = :categoriaId AND p.eliminado = false",
           countQuery = "SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.eliminado = false")
    Page<Producto> findByCategoriaIdPaginated(@Param("categoriaId") Long categoriaId, Pageable pageable);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.nombre = :nombre AND p.eliminado = false")
    List<Producto> findByNombreAndEliminadoFalse(@Param("nombre") String nombre);
}
