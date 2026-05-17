package com.foodstore.config;

import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nombre("Admin")
                    .apellido("Sistema")
                    .email("admin@admin.com")
                    .password(passwordEncoder.encode("123456"))
                    .rol(Rol.ADMIN)
                    .build();

            usuarioRepository.save(admin);
            log.info("✅ Admin creado: admin@admin.com / 123456");
        } else {
            log.info("ℹ️ Ya existen usuarios en BD, se omite seed data");
        }
    }
}
