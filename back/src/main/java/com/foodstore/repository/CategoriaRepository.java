package com.foodstore.repository;

import com.foodstore.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaRepository extends BaseRepository<Categoria, Long> {
    boolean existsByNombreAndEliminadoFalse(String nombre);

    @Query("SELECT c FROM Categoria c WHERE c.eliminado = false AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Categoria> search(@Param("search") String search, Pageable pageable);
}
