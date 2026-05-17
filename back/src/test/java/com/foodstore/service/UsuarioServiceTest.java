package com.foodstore.service;

import com.foodstore.dto.request.UsuarioRequest;
import com.foodstore.dto.response.UsuarioResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.exception.ResourceNotFoundException;
import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UsuarioRequest request;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .email("juan@test.com")
                .celular("1234567890")
                .password("encoded-password")
                .rol(Rol.USUARIO)
                .createdAt(LocalDateTime.of(2026, 5, 16, 12, 0))
                .build();

        request = new UsuarioRequest(
                "Juan Actualizado",
                "Perez Actualizado",
                "nuevo@test.com",
                "0987654321",
                "newPassword123",
                Rol.ADMIN
        );
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnAllUsers() {
            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            List<UsuarioResponse> result = usuarioService.findAll();

            assertThat(result).hasSize(1);
            UsuarioResponse response = result.get(0);
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nombre()).isEqualTo("Juan");
            assertThat(response.email()).isEqualTo("juan@test.com");
            assertThat(response.rol()).isEqualTo(Rol.USUARIO);
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        void shouldReturnEmptyListWhenNoUsers() {
            when(usuarioRepository.findAll()).thenReturn(List.of());

            List<UsuarioResponse> result = usuarioService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldReturnUserWhenExists() {
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);

            UsuarioResponse result = usuarioService.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.nombre()).isEqualTo("Juan");
            assertThat(result.email()).isEqualTo("juan@test.com");
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(usuarioRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Usuario", "id", "999"));

            assertThatThrownBy(() -> usuarioService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario");
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateAllFields() {
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-password");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            UsuarioResponse result = usuarioService.update(1L, request);

            assertThat(result.nombre()).isEqualTo("Juan Actualizado");
            assertThat(result.apellido()).isEqualTo("Perez Actualizado");
            assertThat(result.email()).isEqualTo("nuevo@test.com");

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            Usuario saved = captor.getValue();
            assertThat(saved.getNombre()).isEqualTo("Juan Actualizado");
            assertThat(saved.getCelular()).isEqualTo("0987654321");
            assertThat(saved.getRol()).isEqualTo(Rol.ADMIN);
        }

        @Test
        void shouldUpdatePartiallyWithNullFields() {
            UsuarioRequest partialRequest = new UsuarioRequest(
                    null, null, null, null, null, null
            );

            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            UsuarioResponse result = usuarioService.update(1L, partialRequest);

            assertThat(result.nombre()).isEqualTo("Juan");
            assertThat(result.email()).isEqualTo("juan@test.com");

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.update(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("email ya está registrado");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        void shouldNotThrowWhenUpdatingToSameEmail() {
            UsuarioRequest sameEmailRequest = new UsuarioRequest(
                    "Juan", null, "juan@test.com", null, null, null
            );

            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            UsuarioResponse result = usuarioService.update(1L, sameEmailRequest);

            assertThat(result.email()).isEqualTo("juan@test.com");
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(usuarioRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Usuario", "id", "999"));

            assertThatThrownBy(() -> usuarioService.update(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void shouldSoftDeleteUser() {
            when(usuarioRepository.findByIdOrThrow(1L)).thenReturn(usuario);
            doNothing().when(usuarioRepository).deleteById(1L);

            usuarioService.deleteById(1L);

            verify(usuarioRepository).deleteById(1L);
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(usuarioRepository.findByIdOrThrow(999L))
                    .thenThrow(new ResourceNotFoundException("Usuario", "id", "999"));

            assertThatThrownBy(() -> usuarioService.deleteById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
