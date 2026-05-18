package com.foodstore.config;

import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataLoader(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:admin@admin.com}") String adminEmail,
            @Value("${app.admin.password:123456}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nombre("Admin")
                    .apellido("Sistema")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .rol(Rol.ADMIN)
                    .build();

            usuarioRepository.save(admin);
            log.info("Admin creado con email: {}. Consulte la fuente de configuración para la contraseña.", adminEmail);
        } else {
            log.info("Ya existen usuarios en BD, se omite seed data");
        }
    }
}
