package com.foodstore.config;

import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader(usuarioRepository, passwordEncoder, " Admin@Admin.COM ", "123456");
    }

    @Test
    void shouldCreateAdminWhenNoUsersExist() throws Exception {
        when(usuarioRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-123456");

        dataLoader.run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario admin = captor.getValue();
        assertThat(admin.getEmail()).isEqualTo("admin@admin.com");
        assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void shouldNotCreateAdminWhenUsersExist() throws Exception {
        when(usuarioRepository.count()).thenReturn(1L);

        dataLoader.run();

        verify(usuarioRepository, never()).save(any());
    }
}
