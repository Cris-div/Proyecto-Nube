package com.Lab06.demo.service.impl;

import com.Lab06.demo.dto.LoginRequest;
import com.Lab06.demo.dto.LoginResponse;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.UsuarioRepository;
import com.Lab06.demo.security.authentication.JwtService;
import com.Lab06.demo.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByCorreo(request.getCorreo())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Correo o contraseña incorrectos"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword())) {

            throw new RuntimeException(
                    "Correo o contraseña incorrectos"
            );
        }

        if (!"ACTIVO".equalsIgnoreCase(
                usuario.getEstado())) {

            throw new RuntimeException(
                    "El usuario está inactivo"
            );
        }

        String token =
                jwtService.generarToken(usuario.getCorreo());

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().getNombre()
        );
    }
}