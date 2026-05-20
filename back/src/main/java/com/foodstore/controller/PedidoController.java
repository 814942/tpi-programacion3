package com.foodstore.controller;

import com.foodstore.dto.request.EstadoRequest;
import com.foodstore.dto.request.PedidoRequest;
import com.foodstore.dto.response.PaginatedResponse;
import com.foodstore.dto.response.PedidoResponse;
import com.foodstore.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@Slf4j
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping("/usuario")
    public ResponseEntity<PaginatedResponse<PedidoResponse>> findByUsuario(
            @PageableDefault(size = 20, sort = "fecha") Pageable pageable,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        Long currentUserId = extractCurrentUserId(authentication);
        return ResponseEntity.ok(pedidoService.findByUsuario(currentUserId, pageable, search));
    }

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<PedidoResponse> create(@RequestBody @Valid PedidoRequest request,
                                                  Authentication authentication) {
        Long currentUserId = extractCurrentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.create(currentUserId, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<PedidoResponse>> findAll(
            @PageableDefault(size = 20, sort = "fecha") Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(pedidoService.findAll(pageable, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> findById(@PathVariable Long id,
                                                    Authentication authentication) {
        Long currentUserId = extractCurrentUserId(authentication);
        return ResponseEntity.ok(pedidoService.findById(id, currentUserId));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> updateEstado(@PathVariable Long id,
                                                        @RequestBody @Valid EstadoRequest request) {
        return ResponseEntity.ok(pedidoService.updateEstado(id, request.estado()));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id,
                                                    Authentication authentication) {
        Long currentUserId = extractCurrentUserId(authentication);
        return ResponseEntity.ok(pedidoService.cancelar(id, currentUserId));
    }

    private Long extractCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
