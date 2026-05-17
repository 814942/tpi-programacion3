package com.foodstore.repository;

import com.foodstore.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends BaseRepository<Usuario, Long> {
    Optional<Usuario> findByEmailAndEliminadoFalse(String email);
    boolean existsByEmailAndEliminadoFalse(String email);
}
