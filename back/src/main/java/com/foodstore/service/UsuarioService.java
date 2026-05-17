package com.foodstore.service;

import com.foodstore.dto.request.UsuarioRequest;
import com.foodstore.dto.response.UsuarioResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public UsuarioResponse create(UsuarioRequest request) {
        if (usuarioRepository.existsByEmailAndEliminadoFalse(request.email())) {
            throw new BusinessException("El email ya está registrado");
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .celular(request.celular())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol() != null ? request.rol() : Rol.USUARIO)
                .build();
        usuarioRepository.save(usuario);
        log.info("Usuario {} creado", usuario.getId());
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findByIdOrThrow(id);

        if (request.nombre() != null) usuario.setNombre(request.nombre());
        if (request.apellido() != null) usuario.setApellido(request.apellido());
        if (request.email() != null) {
            if (usuarioRepository.existsByEmailAndEliminadoFalse(request.email()) &&
                !usuario.getEmail().equals(request.email())) {
                throw new BusinessException("El email ya está registrado");
            }
            usuario.setEmail(request.email());
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
    public void deleteById(Long id) {
        usuarioRepository.findByIdOrThrow(id);
        usuarioRepository.deleteById(id);
        log.info("Usuario {} eliminado (soft delete)", id);
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
