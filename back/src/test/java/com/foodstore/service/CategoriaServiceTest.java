package com.foodstore.service;

import com.foodstore.dto.request.CategoriaRequest;
import com.foodstore.dto.request.UpdateCategoriaRequest;
import com.foodstore.dto.response.CategoriaResponse;
import com.foodstore.dto.response.PaginatedResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.Categoria;
import com.foodstore.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Hamburguesas")
                .descripcion("Hamburguesas clásicas")
                .imagen("https://ejemplo.com/hamburguesa.jpg")
                .build();
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnAllCategories() {
            Page<Categoria> page = new PageImpl<>(List.of(categoria));
            when(categoriaRepository.findAll(any(Pageable.class))).thenReturn(page);

            PaginatedResponse<CategoriaResponse> result = categoriaService.findAll(Pageable.unpaged(), null);

            assertThat(result.content()).hasSize(1);
            CategoriaResponse response = result.content().get(0);
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nombre()).isEqualTo("Hamburguesas");
            assertThat(response.descripcion()).isEqualTo("Hamburguesas clásicas");
            assertThat(response.imagen()).isEqualTo("https://ejemplo.com/hamburguesa.jpg");
        }

        @Test
        void shouldReturnEmptyListWhenNoCategories() {
            Page<Categoria> page = new PageImpl<>(List.of());
            when(categoriaRepository.findAll(any(Pageable.class))).thenReturn(page);

            PaginatedResponse<CategoriaResponse> result = categoriaService.findAll(Pageable.unpaged(), null);

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldReturnCategoryWhenExists() {
            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);

            CategoriaResponse result = categoriaService.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.nombre()).isEqualTo("Hamburguesas");
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(categoriaRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            assertThatThrownBy(() -> categoriaService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Categoria");
        }
    }

    @Nested
    class CreateTests {

        private final CategoriaRequest createRequest = new CategoriaRequest(
                "Nueva Categoría",
                "Descripción",
                "https://ejemplo.com/img.jpg"
        );

        @Test
        void shouldCreateCategory() {
            when(categoriaRepository.existsByNombreAndEliminadoFalse("Nueva Categoría")).thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

            CategoriaResponse result = categoriaService.create(createRequest);

            assertThat(result).isNotNull();
            ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
            verify(categoriaRepository).save(captor.capture());
            Categoria saved = captor.getValue();
            assertThat(saved.getNombre()).isEqualTo("Nueva Categoría");
            assertThat(saved.getDescripcion()).isEqualTo("Descripción");
            assertThat(saved.getImagen()).isEqualTo("https://ejemplo.com/img.jpg");
        }

        @Test
        void shouldThrowWhenDuplicateName() {
            when(categoriaRepository.existsByNombreAndEliminadoFalse("Nueva Categoría")).thenReturn(true);

            assertThatThrownBy(() -> categoriaService.create(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre de categoría ya existe");

            verify(categoriaRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateTests {

        private final UpdateCategoriaRequest updateRequest = new UpdateCategoriaRequest(
                "Actualizada",
                "Nueva descripción",
                "https://ejemplo.com/nueva.jpg"
        );

        @Test
        void shouldUpdateAllFields() {
            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            when(categoriaRepository.existsByNombreAndEliminadoFalse("Actualizada")).thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

            CategoriaResponse result = categoriaService.update(1L, updateRequest);

            assertThat(result.nombre()).isEqualTo("Actualizada");
            assertThat(result.descripcion()).isEqualTo("Nueva descripción");
            assertThat(result.imagen()).isEqualTo("https://ejemplo.com/nueva.jpg");

            ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
            verify(categoriaRepository).save(captor.capture());
            Categoria saved = captor.getValue();
            assertThat(saved.getNombre()).isEqualTo("Actualizada");
            assertThat(saved.getDescripcion()).isEqualTo("Nueva descripción");
            assertThat(saved.getImagen()).isEqualTo("https://ejemplo.com/nueva.jpg");
        }

        @Test
        void shouldUpdatePartially() {
            UpdateCategoriaRequest partialRequest = new UpdateCategoriaRequest(
                    null, null, null
            );

            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

            CategoriaResponse result = categoriaService.update(1L, partialRequest);

            assertThat(result.nombre()).isEqualTo("Hamburguesas");
            assertThat(result.descripcion()).isEqualTo("Hamburguesas clásicas");
            assertThat(result.imagen()).isEqualTo("https://ejemplo.com/hamburguesa.jpg");
        }

        @Test
        void shouldThrowWhenDuplicateName() {
            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            when(categoriaRepository.existsByNombreAndEliminadoFalse("Actualizada")).thenReturn(true);

            assertThatThrownBy(() -> categoriaService.update(1L, updateRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nombre de categoría ya existe");

            verify(categoriaRepository, never()).save(any());
        }

        @Test
        void shouldNotThrowWhenUpdatingToSameName() {
            UpdateCategoriaRequest sameNameRequest = new UpdateCategoriaRequest(
                    "Hamburguesas", null, null
            );

            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

            CategoriaResponse result = categoriaService.update(1L, sameNameRequest);

            assertThat(result.nombre()).isEqualTo("Hamburguesas");
        }

        @Test
        void shouldThrowWhenCategoryNotFound() {
            when(categoriaRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            assertThatThrownBy(() -> categoriaService.update(999L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void shouldSoftDeleteCategory() {
            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            doNothing().when(categoriaRepository).deleteById(1L);

            categoriaService.deleteById(1L);

            verify(categoriaRepository).deleteById(1L);
        }

        @Test
        void shouldThrowWhenCategoryNotFound() {
            when(categoriaRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            assertThatThrownBy(() -> categoriaService.deleteById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
