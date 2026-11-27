
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.UsuarioRepository;
import com.reinodeloscielos.backend.service.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
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

        // Validar longitud máxima de contraseña para BCrypt.
        if (!request.tieneLongitudValida()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "mensaje",
                            "La contraseña supera la longitud máxima permitida"
                    ));
        }

        try {

            // Normalizar nombre de usuario.
            String username = request.username()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            // Autenticar credenciales.
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    request.password()
                            )
                    );

            // Recuperar información del usuario autenticado.
            Usuario usuario = usuarioRepository
                    .findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontró el usuario autenticado"
                    ));

            // Generar token JWT.
            String token = jwtService.generarToken(
                    usuario.getUsername(),
                    usuario.getRol()
            );

            // Responder al frontend.
            return ResponseEntity.ok(
                    Map.of(
                            "mensaje", "Inicio de sesión correcto",
                            "username", usuario.getUsername(),
                            "rol", usuario.getRol(),
                            "token", token
                    )
            );

        } catch (AuthenticationException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Usuario o contraseña incorrectos"
                    ));
        }
    }

    public record LoginRequest(

            @NotBlank(message = "El nombre de usuario es obligatorio")
            @Size(
                    min = 4,
                    max = 100,
                    message = "El nombre de usuario debe tener entre 4 y 100 caracteres"
            )
            @Pattern(
                    regexp = "^[a-zA-Z0-9._-]+$",
                    message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos"
            )
            String username,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(
                    min = 8,
                    message = "La contraseña debe tener al menos 8 caracteres"
            )
            String password

    ) {
        public boolean tieneLongitudValida() {
            return password != null &&
                    password.getBytes(StandardCharsets.UTF_8).length <= 72;
        }
    }
}
