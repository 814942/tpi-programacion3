package com.foodstore.service;

import com.foodstore.dto.request.LoginRequest;
import com.foodstore.dto.request.RegisterRequest;
import com.foodstore.dto.response.AuthResponse;
import com.foodstore.exception.BusinessException;
import com.foodstore.model.Usuario;
import com.foodstore.model.enums.Rol;
import com.foodstore.repository.UsuarioRepository;
import com.foodstore.security.JwtProvider;
import com.foodstore.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthResponse login(LoginRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        Usuario usuario = usuarioRepository.findByEmailAndEliminadoFalse(email)
                .orElseThrow(() -> new BusinessException("Email o contraseña inválidos"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BusinessException("Email o contraseña inválidos");
        }

        String token = jwtProvider.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol().name());
        log.info("Login exitoso: {}", usuario.getEmail());
        return toAuthResponse(token, usuario);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        if (usuarioRepository.existsByEmailAndEliminadoFalse(email)) {
            throw new BusinessException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(email)
                .celular(request.celular())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.USUARIO)
                .build();

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario registrado: {}", usuario.getEmail());

        String token = jwtProvider.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol().name());
        return toAuthResponse(token, usuario);
    }

    private AuthResponse toAuthResponse(String token, Usuario usuario) {
        return new AuthResponse(
                token, usuario.getId(),
                usuario.getEmail(), usuario.getNombre(),
                usuario.getApellido(), usuario.getCelular(),
                usuario.getRol()
        );
    }
}
