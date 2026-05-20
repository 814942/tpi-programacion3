package com.foodstore.controller;

import com.foodstore.dto.request.ProductoRequest;
import com.foodstore.dto.request.UpdateProductoRequest;
import com.foodstore.dto.response.PaginatedResponse;
import com.foodstore.dto.response.ProductoResponse;
import com.foodstore.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Slf4j
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductoResponse>> findAll(
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productoService.findAll(pageable, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<PaginatedResponse<ProductoResponse>> findByCategoriaId(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productoService.findByCategoriaId(id, pageable, search));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> create(@RequestBody @Valid ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateProductoRequest request) {
        return ResponseEntity.ok(productoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
