package com.foodstore.repository;

import com.foodstore.model.Categoria;

public interface CategoriaRepository extends BaseRepository<Categoria, Long> {
    boolean existsByNombreAndEliminadoFalse(String nombre);
}
