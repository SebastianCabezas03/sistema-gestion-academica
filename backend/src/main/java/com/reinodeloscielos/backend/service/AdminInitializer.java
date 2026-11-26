
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner crearAdministrador(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String username = "admin";

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
                System.err.println(
                        "No se creó el administrador: configura ADMIN_PASSWORD y reinicia."
                );
                return;
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
