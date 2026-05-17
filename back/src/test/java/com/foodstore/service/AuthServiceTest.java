package com.foodstore.service;

import com.foodstore.dto.request.LoginRequest;
import com.foodstore.dto.request.RegisterRequest;
import com.foodstore.dto.response.AuthResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import com.foodstore.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

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
                .build();

        loginRequest = new LoginRequest("juan@test.com", "password123");

        registerRequest = new RegisterRequest(
                "Juan", "Perez", "nuevo@test.com", "1234567890", "password123"
        );
    }

    @Nested
    class LoginTests {

        @Test
        void shouldReturnAuthResponseWhenCredentialsAreValid() {
            when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.com"))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
            when(jwtProvider.generateToken(1L, "juan@test.com", "USUARIO")).thenReturn("jwt-token");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token");
            assertThat(response.type()).isEqualTo("Bearer");
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("juan@test.com");
            assertThat(response.nombre()).isEqualTo("Juan");
            assertThat(response.apellido()).isEqualTo("Perez");
            assertThat(response.celular()).isEqualTo("1234567890");
            assertThat(response.role()).isEqualTo(Rol.USUARIO);
        }

        @Test
        void shouldThrowWhenEmailDoesNotExist() {
            when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email o contraseña inválidos");
        }

        @Test
        void shouldThrowWhenPasswordIsIncorrect() {
            when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.com"))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email o contraseña inválidos");
        }

        @Test
        void shouldAuthenticateWithUppercaseEmailInput() {
            LoginRequest upperCaseRequest = new LoginRequest("JUAN@TEST.COM", "password123");
            when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.com"))
                    .thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
            when(jwtProvider.generateToken(1L, "juan@test.com", "USUARIO")).thenReturn("jwt-token");

            AuthResponse response = authService.login(upperCaseRequest);

            assertThat(response.token()).isEqualTo("jwt-token");
            verify(usuarioRepository).findByEmailAndEliminadoFalse("juan@test.com");
        }

        @Test
        void shouldNotRevealWhichFieldIsInvalid() {
            when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email o contraseña inválidos");
        }
    }

    @Nested
    class RegisterTests {

        @Test
        void shouldCreateUserAndReturnAuthResponse() {
            when(usuarioRepository.existsByEmailAndEliminadoFalse("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(jwtProvider.generateToken(1L, "juan@test.com", "USUARIO")).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token");
            assertThat(response.type()).isEqualTo("Bearer");
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("juan@test.com");
            assertThat(response.nombre()).isEqualTo("Juan");
            assertThat(response.role()).isEqualTo(Rol.USUARIO);

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            Usuario saved = captor.getValue();
            assertThat(saved.getNombre()).isEqualTo("Juan");
            assertThat(saved.getApellido()).isEqualTo("Perez");
            assertThat(saved.getEmail()).isEqualTo("nuevo@test.com");
            assertThat(saved.getCelular()).isEqualTo("1234567890");
            assertThat(saved.getPassword()).isEqualTo("encoded-password");
            assertThat(saved.getRol()).isEqualTo(Rol.USUARIO);
        }

        @Test
        void shouldLowercaseEmailWhenRegistering() {
            RegisterRequest upperCaseRequest = new RegisterRequest(
                    "Juan", "Perez", "NUEVO@TEST.COM", null, "password123"
            );

            when(usuarioRepository.existsByEmailAndEliminadoFalse("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(jwtProvider.generateToken(anyLong(), anyString(), anyString())).thenReturn("jwt");

            authService.register(upperCaseRequest);

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getEmail()).isEqualTo("nuevo@test.com");
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            when(usuarioRepository.existsByEmailAndEliminadoFalse("nuevo@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("El email ya está registrado");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        void shouldAlwaysCreateUserWithRolUsuario() {
            when(usuarioRepository.existsByEmailAndEliminadoFalse("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(jwtProvider.generateToken(anyLong(), anyString(), anyString())).thenReturn("jwt");

            authService.register(registerRequest);

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getRol()).isEqualTo(Rol.USUARIO);
        }

        @Test
        void shouldNotReturnPasswordInResponse() {
            when(usuarioRepository.existsByEmailAndEliminadoFalse("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(jwtProvider.generateToken(1L, "juan@test.com", "USUARIO")).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            // AuthResponse doesn't have a password field — verify no method exists
            assertThat(response).isNotNull();
            // Verify we can't access password via reflection-like check
            assertThat(response.getClass().getRecordComponents())
                    .noneMatch(c -> c.getName().equals("password"));
        }
    }
}
