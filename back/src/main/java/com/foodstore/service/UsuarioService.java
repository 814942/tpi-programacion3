package com.foodstore.service;

import com.foodstore.dto.request.CreateUsuarioRequest;
import com.foodstore.dto.request.UpdateUsuarioRequest;
import com.foodstore.dto.response.UsuarioResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.Usuario;
import com.foodstore.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponse findById(Long id) {
        Usuario usuario = usuarioRepository.findByIdOrThrow(id);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse create(CreateUsuarioRequest request) {
        if (request.rol() == null) {
            throw new BusinessException("El rol es obligatorio");
        }

        String email = normalizeEmail(request.email());
        if (usuarioRepository.existsByEmailAndEliminadoFalse(email)) {
            throw new BusinessException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(email)
                .celular(request.celular())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario {} creado", usuario.getId());
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse update(Long id, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findByIdOrThrow(id);

        if (request.nombre() != null) usuario.setNombre(request.nombre());
        if (request.apellido() != null) usuario.setApellido(request.apellido());
        if (request.email() != null) {
            String normalizedEmail = normalizeEmail(request.email());
            if (usuarioRepository.existsByEmailAndEliminadoFalse(normalizedEmail) &&
                !usuario.getEmail().equals(normalizedEmail)) {
                throw new BusinessException("El email ya está registrado");
            }
            usuario.setEmail(normalizedEmail);
        }
        if (request.celular() != null) usuario.setCelular(request.celular());
        if (request.password() != null) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.rol() != null) usuario.setRol(request.rol());

        usuarioRepository.save(usuario);
        log.info("Usuario {} actualizado", id);
        return toResponse(usuario);
    }

    @Transactional
    public void deleteById(Long id, Long currentUserId) {
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new AccessDeniedException("No podés eliminar tu propio usuario");
        }

        usuarioRepository.findByIdOrThrow(id);
        usuarioRepository.deleteById(id);
        log.info("Usuario {} eliminado (soft delete)", id);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getCelular(),
                usuario.getRol(),
                usuario.getCreatedAt()
        );
    }
}
