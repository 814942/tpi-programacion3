package com.foodstore.service;

import com.foodstore.dto.request.ProductoRequest;
import com.foodstore.dto.request.UpdateProductoRequest;
import com.foodstore.dto.response.CategoriaResponse;
import com.foodstore.dto.response.PaginatedResponse;
import com.foodstore.dto.response.ProductoResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.Categoria;
import com.foodstore.model.Producto;
import com.foodstore.repository.CategoriaRepository;
import com.foodstore.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodstore.dto.response.ProductoValidacionResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> findAll() {
        return productoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductoResponse> findAll(Pageable pageable, String search) {
        Page<Producto> page;
        if (search != null && !search.isBlank()) {
            page = productoRepository.searchByNombre(search, pageable);
        } else {
            page = productoRepository.findAll(pageable);
        }
        return PaginatedResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductoResponse findById(Long id) {
        Producto producto = productoRepository.findByIdOrThrow(id);
        return toResponse(producto);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductoResponse> findByCategoriaId(Long categoriaId, Pageable pageable, String search) {
        categoriaRepository.findByIdOrThrow(categoriaId);
        Page<Producto> page;
        if (search != null && !search.isBlank()) {
            page = productoRepository.searchByCategoriaId(categoriaId, search, pageable);
        } else {
            page = productoRepository.findByCategoriaIdPaginated(categoriaId, pageable);
        }
        return PaginatedResponse.from(page.map(this::toResponse));
    }

    @Transactional
    public ProductoResponse create(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findByIdOrThrow(request.idCategoria());

        if (request.precio().compareTo(new BigDecimal("0.01")) <= 0) {
            throw new BusinessException("El precio debe ser mayor a 0.01");
        }

        boolean disponible = request.disponible() != null ? request.disponible() : true;

        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .precio(request.precio())
                .descripcion(request.descripcion())
                .stock(request.stock())
                .imagen(request.imagen())
                .disponible(disponible)
                .categoria(categoria)
                .build();

        productoRepository.save(producto);
        log.info("Producto {} creado: {}", producto.getId(), producto.getNombre());
        return toResponse(producto);
    }

    @Transactional
    public ProductoResponse update(Long id, UpdateProductoRequest request) {
        Producto producto = productoRepository.findByIdOrThrow(id);

        if (request.nombre() != null) {
            producto.setNombre(request.nombre());
        }
        if (request.precio() != null) {
            if (request.precio().compareTo(new BigDecimal("0.01")) <= 0) {
                throw new BusinessException("El precio debe ser mayor a 0.01");
            }
            producto.setPrecio(request.precio());
        }
        if (request.descripcion() != null) {
            producto.setDescripcion(request.descripcion());
        }
        if (request.stock() != null) {
            producto.setStock(request.stock());
        }
        if (request.imagen() != null) {
            producto.setImagen(request.imagen());
        }
        if (request.disponible() != null) {
            producto.setDisponible(request.disponible());
        }
        if (request.idCategoria() != null) {
            Categoria categoria = categoriaRepository.findByIdOrThrow(request.idCategoria());
            producto.setCategoria(categoria);
        }

        productoRepository.save(producto);
        log.info("Producto {} actualizado", id);
        return toResponse(producto);
    }

    @Transactional
    public void deleteById(Long id) {
        productoRepository.findByIdOrThrow(id);
        productoRepository.deleteById(id);
        log.info("Producto {} eliminado (soft delete)", id);
    }

    @Transactional(readOnly = true)
    public List<ProductoValidacionResponse> validarProductos(List<Long> ids) {
        List<Producto> productos = productoRepository.findAllById(ids);
        Set<Long> encontrados = productos.stream()
            .map(Producto::getId)
            .collect(Collectors.toSet());

        return ids.stream().map(id -> {
            if (!encontrados.contains(id)) {
                return new ProductoValidacionResponse(id, false, false, 0);
            }
            Producto p = productoRepository.findById(id).orElse(null);
            if (p == null) {
                return new ProductoValidacionResponse(id, false, false, 0);
            }
            boolean disponible = p.getDisponible() != null && p.getDisponible() && !p.isEliminado();
            return new ProductoValidacionResponse(id, true, disponible, disponible ? p.getStock() : 0);
        }).collect(Collectors.toList());
    }

    private ProductoResponse toResponse(Producto producto) {
        Categoria cat = producto.getCategoria();
        CategoriaResponse catResponse = new CategoriaResponse(
                cat.getId(),
                cat.getNombre(),
                cat.getDescripcion(),
                cat.getImagen()
        );

        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getDescripcion(),
                producto.getStock(),
                producto.getImagen(),
                producto.getDisponible(),
                catResponse,
                producto.getCreatedAt(),
                producto.getUpdatedAt()
        );
    }
}
