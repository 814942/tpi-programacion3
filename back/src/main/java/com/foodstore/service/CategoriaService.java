package com.foodstore.service;

import com.foodstore.dto.request.CategoriaRequest;
import com.foodstore.dto.request.UpdateCategoriaRequest;
import com.foodstore.dto.response.CategoriaResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.Categoria;
import com.foodstore.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> findAll() {
        return categoriaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoriaResponse findById(Long id) {
        Categoria categoria = categoriaRepository.findByIdOrThrow(id);
        return toResponse(categoria);
    }

    @Transactional
    public CategoriaResponse create(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreAndEliminadoFalse(request.nombre())) {
            throw new BusinessException("El nombre de categoría ya existe");
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .imagen(request.imagen())
                .build();

        categoriaRepository.save(categoria);
        log.info("Categoría {} creada: {}", categoria.getId(), categoria.getNombre());
        return toResponse(categoria);
    }

    @Transactional
    public CategoriaResponse update(Long id, UpdateCategoriaRequest request) {
        Categoria categoria = categoriaRepository.findByIdOrThrow(id);

        if (request.nombre() != null) {
            if (!request.nombre().equals(categoria.getNombre()) &&
                categoriaRepository.existsByNombreAndEliminadoFalse(request.nombre())) {
                throw new BusinessException("El nombre de categoría ya existe");
            }
            categoria.setNombre(request.nombre());
        }
        if (request.descripcion() != null) {
            categoria.setDescripcion(request.descripcion());
        }
        if (request.imagen() != null) {
            categoria.setImagen(request.imagen());
        }

        categoriaRepository.save(categoria);
        log.info("Categoría {} actualizada", id);
        return toResponse(categoria);
    }

    @Transactional
    public void deleteById(Long id) {
        categoriaRepository.findByIdOrThrow(id);
        categoriaRepository.deleteById(id);
        log.info("Categoría {} eliminada (soft delete)", id);
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getImagen()
        );
    }
}
