package com.foodstore.controller;

import com.foodstore.dto.request.CreateUsuarioRequest;
import com.foodstore.dto.request.UpdateUsuarioRequest;
import com.foodstore.dto.response.UsuarioResponse;
import com.foodstore.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@RequestBody @Valid CreateUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Long id, Authentication authentication) {
        usuarioService.deleteById(id, extractCurrentUserId(authentication));
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
    }

    private Long extractCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof Integer userId) {
            return userId.longValue();
        }
        if (principal instanceof String rawValue) {
            try {
                return Long.parseLong(rawValue);
            } catch (NumberFormatException ex) {
                log.warn("Principal inválido para extracción de ID de usuario: {}", rawValue);
                throw new AccessDeniedException("No se pudo validar la identidad del usuario autenticado");
            }
        }
        log.warn("Tipo de principal no soportado para extracción de ID: {}", principal.getClass().getName());
        throw new AccessDeniedException("No se pudo validar la identidad del usuario autenticado");
    }
}
