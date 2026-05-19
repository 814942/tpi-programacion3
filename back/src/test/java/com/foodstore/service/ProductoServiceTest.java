package com.foodstore.service;

import com.foodstore.dto.request.ProductoRequest;
import com.foodstore.dto.request.UpdateProductoRequest;
import com.foodstore.dto.response.CategoriaResponse;
import com.foodstore.dto.response.ProductoResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.Categoria;
import com.foodstore.model.Producto;
import com.foodstore.repository.CategoriaRepository;
import com.foodstore.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;
    private ProductoRequest createRequest;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Hamburguesas")
                .descripcion("Hamburguesas clásicas")
                .imagen("https://ejemplo.com/hamburguesa.jpg")
                .build();

        producto = Producto.builder()
                .id(1L)
                .nombre("Clásica")
                .precio(new BigDecimal("25000.00"))
                .descripcion("Hamburguesa clásica")
                .stock(50)
                .imagen("https://ejemplo.com/clasica.jpg")
                .disponible(true)
                .categoria(categoria)
                .build();

        createRequest = new ProductoRequest(
                "Clásica",
                new BigDecimal("25000.00"),
                "Hamburguesa clásica",
                50,
                "https://ejemplo.com/clasica.jpg",
                true,
                1L
        );
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnAllProducts() {
            when(productoRepository.findAll()).thenReturn(List.of(producto));

            List<ProductoResponse> result = productoService.findAll();

            assertThat(result).hasSize(1);
            ProductoResponse response = result.get(0);
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nombre()).isEqualTo("Clásica");
            assertThat(response.precio()).isEqualByComparingTo(new BigDecimal("25000.00"));
            assertThat(response.stock()).isEqualTo(50);
            assertThat(response.disponible()).isTrue();
            assertThat(response.categoria().id()).isEqualTo(1L);
            assertThat(response.categoria().nombre()).isEqualTo("Hamburguesas");
        }

        @Test
        void shouldReturnEmptyListWhenNoProducts() {
            when(productoRepository.findAll()).thenReturn(List.of());

            List<ProductoResponse> result = productoService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldReturnProductWhenExists() {
            when(productoRepository.findByIdOrThrow(1L)).thenReturn(producto);

            ProductoResponse result = productoService.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.nombre()).isEqualTo("Clásica");
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(productoRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Producto", "id", "999"));

            assertThatThrownBy(() -> productoService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Producto");
        }
    }

    @Nested
    class FindByCategoriaIdTests {

        @Test
        void shouldReturnProductsWhenCategoriaExists() {
            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(producto));

            List<ProductoResponse> result = productoService.findByCategoriaId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nombre()).isEqualTo("Clásica");
        }

        @Test
        void shouldThrowWhenCategoriaNotFound() {
            when(categoriaRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            assertThatThrownBy(() -> productoService.findByCategoriaId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Categoria");
        }

        @Test
        void shouldReturnEmptyListWhenCategoriaHasNoProducts() {
            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of());

            List<ProductoResponse> result = productoService.findByCategoriaId(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class CreateTests {

        @Test
        void shouldCreateProduct() {
            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            when(productoRepository.save(any(Producto.class))).thenReturn(producto);

            ProductoResponse result = productoService.create(createRequest);

            assertThat(result).isNotNull();
            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productoRepository).save(captor.capture());
            Producto saved = captor.getValue();
            assertThat(saved.getNombre()).isEqualTo("Clásica");
            assertThat(saved.getPrecio()).isEqualByComparingTo(new BigDecimal("25000.00"));
            assertThat(saved.getDescripcion()).isEqualTo("Hamburguesa clásica");
            assertThat(saved.getStock()).isEqualTo(50);
            assertThat(saved.getImagen()).isEqualTo("https://ejemplo.com/clasica.jpg");
            assertThat(saved.getDisponible()).isTrue();
            assertThat(saved.getCategoria().getId()).isEqualTo(1L);
        }

        @Test
        void shouldCreateProductWithDefaultDisponible() {
            ProductoRequest requestWithoutDisponible = new ProductoRequest(
                    "Clásica",
                    new BigDecimal("25000.00"),
                    "Hamburguesa clásica",
                    50,
                    "https://ejemplo.com/clasica.jpg",
                    null,
                    1L
            );

            when(categoriaRepository.findByIdOrThrow(1L)).thenReturn(categoria);
            Producto savedProduct = Producto.builder()
                    .id(2L)
                    .nombre("Clásica")
                    .precio(new BigDecimal("25000.00"))
                    .stock(50)
                    .disponible(true)
                    .categoria(categoria)
                    .build();
            when(productoRepository.save(any(Producto.class))).thenReturn(savedProduct);

            ProductoResponse result = productoService.create(requestWithoutDisponible);

            assertThat(result.disponible()).isTrue();
            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productoRepository).save(captor.capture());
            assertThat(captor.getValue().getDisponible()).isTrue();
        }

        @Test
        void shouldThrowWhenPrecioIsZeroOrLess() {
            ProductoRequest request = new ProductoRequest(
                    "Clásica",
                    new BigDecimal("0.001"),
                    "Hamburguesa clásica",
                    50,
                    "https://ejemplo.com/clasica.jpg",
                    true,
                    1L
            );

            assertThatThrownBy(() -> productoService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("precio debe ser mayor");

            verify(productoRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenCategoriaNotFound() {
            when(categoriaRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Categoria", "id", "999"));

            ProductoRequest request = new ProductoRequest(
                    "Clásica",
                    new BigDecimal("25000.00"),
                    "Hamburguesa clásica",
                    50,
                    "https://ejemplo.com/clasica.jpg",
                    true,
                    999L
            );

            assertThatThrownBy(() -> productoService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Categoria");

            verify(productoRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateTests {

        private final UpdateProductoRequest updateRequest = new UpdateProductoRequest(
                "Actualizada",
                new BigDecimal("30000.00"),
                "Nueva descripción",
                20,
                "https://ejemplo.com/nueva.jpg",
                false,
                null
        );

        @Test
        void shouldUpdateAllFields() {
            when(productoRepository.findByIdOrThrow(1L)).thenReturn(producto);
            when(productoRepository.save(any(Producto.class))).thenReturn(producto);

            ProductoResponse result = productoService.update(1L, updateRequest);

            assertThat(result.nombre()).isEqualTo("Actualizada");
            assertThat(result.precio()).isEqualByComparingTo(new BigDecimal("30000.00"));
            assertThat(result.descripcion()).isEqualTo("Nueva descripción");
            assertThat(result.stock()).isEqualTo(20);
            assertThat(result.imagen()).isEqualTo("https://ejemplo.com/nueva.jpg");
            assertThat(result.disponible()).isFalse();

            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productoRepository).save(captor.capture());
            Producto saved = captor.getValue();
            assertThat(saved.getNombre()).isEqualTo("Actualizada");
            assertThat(saved.getPrecio()).isEqualByComparingTo(new BigDecimal("30000.00"));
            assertThat(saved.getDescripcion()).isEqualTo("Nueva descripción");
            assertThat(saved.getStock()).isEqualTo(20);
            assertThat(saved.getImagen()).isEqualTo("https://ejemplo.com/nueva.jpg");
            assertThat(saved.getDisponible()).isFalse();
        }

        @Test
        void shouldUpdatePartiallyWithNullFields() {
            UpdateProductoRequest partialRequest = new UpdateProductoRequest(
                    null, null, null, null, null, null, null
            );

            when(productoRepository.findByIdOrThrow(1L)).thenReturn(producto);
            when(productoRepository.save(any(Producto.class))).thenReturn(producto);

            ProductoResponse result = productoService.update(1L, partialRequest);

            assertThat(result.nombre()).isEqualTo("Clásica");
            assertThat(result.precio()).isEqualByComparingTo(new BigDecimal("25000.00"));
            assertThat(result.descripcion()).isEqualTo("Hamburguesa clásica");
            assertThat(result.stock()).isEqualTo(50);
            assertThat(result.imagen()).isEqualTo("https://ejemplo.com/clasica.jpg");
            assertThat(result.disponible()).isTrue();
        }

        @Test
        void shouldUpdateCategoria() {
            Categoria nuevaCategoria = Categoria.builder()
                    .id(2L)
                    .nombre("Pizzas")
                    .build();

            UpdateProductoRequest requestWithCategoria = new UpdateProductoRequest(
                    null, null, null, null, null, null, 2L
            );

            when(productoRepository.findByIdOrThrow(1L)).thenReturn(producto);
            when(categoriaRepository.findByIdOrThrow(2L)).thenReturn(nuevaCategoria);
            when(productoRepository.save(any(Producto.class))).thenReturn(producto);

            ProductoResponse result = productoService.update(1L, requestWithCategoria);

            assertThat(result.categoria().id()).isEqualTo(2L);
            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productoRepository).save(captor.capture());
            assertThat(captor.getValue().getCategoria().getId()).isEqualTo(2L);
        }

        @Test
        void shouldThrowWhenProductNotFound() {
            when(productoRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Producto", "id", "999"));

            assertThatThrownBy(() -> productoService.update(999L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void shouldSoftDeleteProduct() {
            when(productoRepository.findByIdOrThrow(1L)).thenReturn(producto);
            doNothing().when(productoRepository).deleteById(1L);

            productoService.deleteById(1L);

            verify(productoRepository).deleteById(1L);
        }

        @Test
        void shouldThrowWhenProductNotFound() {
            when(productoRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Producto", "id", "999"));

            assertThatThrownBy(() -> productoService.deleteById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
