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

            if (!usuarioRepository.existsByUsername(username)) {
            String password = System.getenv("ADMIN_PASSWORD");
            if (password == null || password.isBlank()) {
                System.err.println("No se creó el administrador: configura ADMIN_PASSWORD y reinicia.");
                return;
            }
            Usuario admin = new Usuario();
            admin.setUsername(username);
            admin.setPassword(
                passwordEncoder.encode(password)
            );
            admin.setRol("ADMIN");

                usuarioRepository.save(admin);
                System.out.println(
                        "Administrador inicial creado correctamente."
                );
            }
        };
    }
}
