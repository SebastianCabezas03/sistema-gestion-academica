
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException(
                    "El nombre de usuario es obligatorio"
            );
        }

        String usernameNormalizado = username
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByUsername(usernameNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"
                ));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .build();
    }
}

