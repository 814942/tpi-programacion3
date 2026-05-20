package com.foodstore.repository;

import com.foodstore.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends BaseRepository<Usuario, Long> {
    Optional<Usuario> findByEmailAndEliminadoFalse(String email);
    boolean existsByEmailAndEliminadoFalse(String email);

    @Query("SELECT u FROM Usuario u WHERE u.eliminado = false AND (LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(CONCAT('', u.rol)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Usuario> search(@Param("search") String search, Pageable pageable);
}
