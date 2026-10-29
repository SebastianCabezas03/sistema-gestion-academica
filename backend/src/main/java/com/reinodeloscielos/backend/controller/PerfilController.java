
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.PerfilResponse;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;

    public PerfilController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/me")
    public PerfilResponse obtenerMiPerfil(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión");
        }

        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        if ("ALUMNO".equals(usuario.getRol())) {
            if (usuario.getAlumno() == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cuenta no tiene un alumno vinculado");
            }

            var alumno = usuario.getAlumno();

            return new PerfilResponse(
                    usuario.getUsername(),
                    usuario.getRol(),
                    alumno.getId(),
                    alumno.getNombres(),
                    alumno.getApellidos(),
                    alumno.getCorreo(),
                    alumno.getTelefono(),
                    null
            );
        }

        if ("DOCENTE".equals(usuario.getRol())) {
            if (usuario.getDocente() == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cuenta no tiene un docente vinculado");
            }

            var docente = usuario.getDocente();

            return new PerfilResponse(
                    usuario.getUsername(),
                    usuario.getRol(),
                    docente.getId(),
                    docente.getNombres(),
                    docente.getApellidos(),
                    docente.getCorreo(),
                    docente.getTelefono(),
                    docente.getEspecialidad()
            );
        }

        return new PerfilResponse(
                usuario.getUsername(),
                usuario.getRol(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
