package com.foodstore.config;

import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataLoader dataLoader;

    @Test
    void shouldCreateAdminWhenNoUsersExist() throws Exception {
        when(usuarioRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-123456");

        dataLoader.run();

        verify(usuarioRepository).save(any());
    }

    @Test
    void shouldNotCreateAdminWhenUsersExist() throws Exception {
        when(usuarioRepository.count()).thenReturn(1L);

        dataLoader.run();

        verify(usuarioRepository, never()).save(any());
    }
}
