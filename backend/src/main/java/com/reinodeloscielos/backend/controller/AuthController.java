
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.UsuarioRepository;
import com.reinodeloscielos.backend.service.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        try {

            // Autenticar credenciales
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username().trim(),
                                    request.password()
                            )
                    );

            // Recuperar información del usuario autenticado
            Usuario usuario = usuarioRepository
                    .findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontró el usuario autenticado"
                    ));

            // Generar token JWT
            String token = jwtService.generarToken(
                    usuario.getUsername(),
                    usuario.getRol()
            );

            // Respuesta del inicio de sesión
            return ResponseEntity.ok(
                    Map.of(
                            "mensaje", "Inicio de sesión correcto",
                            "username", usuario.getUsername(),
                            "rol", usuario.getRol(),
                            "token", token
                    )
            );

        } catch (AuthenticationException e) {

            // Credenciales incorrectas
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "Usuario o contraseña incorrectos"
                            )
                    );
        }
    }

    public record LoginRequest(

            @NotBlank(message = "El nombre de usuario es obligatorio")
            String username,

            @NotBlank(message = "La contraseña es obligatoria")
            String password

    ) {
    }
}
