
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner crearAdministrador(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String username = "admin".toLowerCase(Locale.ROOT);

            // Comprobar si ya existe el administrador.
            if (usuarioRepository.existsByUsername(username)) {
                System.out.println(
                        "El administrador inicial ya se encuentra registrado."
                );
                return;
            }

            // Obtener contraseña desde variable de entorno.
            String password = System.getenv("ADMIN_PASSWORD");

            if (password == null || password.isBlank()) {
                throw new IllegalStateException(
                        "No se puede iniciar la aplicación: configura ADMIN_PASSWORD para crear el administrador inicial."
                );
            }

            // Validar longitud de contraseña.
            int passwordBytes = password.getBytes(
                    StandardCharsets.UTF_8
            ).length;

            if (password.length() < 8 || passwordBytes > 72) {
                throw new IllegalStateException(
                        "ADMIN_PASSWORD debe tener al menos 8 caracteres y no superar los 72 bytes."
                );
            }

            // Crear cuenta administrativa.
            Usuario admin = new Usuario();

            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRol("ADMIN");

            // Guardar administrador.
            usuarioRepository.save(admin);

            System.out.println(
                    "Administrador inicial creado correctamente."
            );
        };
    }
}

