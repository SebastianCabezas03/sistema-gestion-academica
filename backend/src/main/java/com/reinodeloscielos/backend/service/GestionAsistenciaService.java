
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.AsistenciaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GestionAsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public GestionAsistenciaService(
            AsistenciaRepository asistenciaRepository,
            UsuarioRepository usuarioRepository) {

        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuario obtenerUsuario(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }

    private Asistencia obtenerAsistencia(Long asistenciaId) {

        return asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro de asistencia no encontrado"
                ));
    }

    private void validarPermiso(
            Usuario usuario,
            Asistencia asistencia) {

        if ("ADMIN".equals(usuario.getRol())) {
            return;
        }

        if (!"DOCENTE".equals(usuario.getRol())
                || usuario.getDocente() == null
                || asistencia.getCurso().getDocente() == null
                || !asistencia.getCurso().getDocente().getId()
                        .equals(usuario.getDocente().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para modificar esta asistencia"
            );
        }
    }

    @Transactional
    public Asistencia actualizar(
            Long asistenciaId,
            Asistencia.Estado nuevoEstado,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        Asistencia asistencia = obtenerAsistencia(asistenciaId);

        validarPermiso(usuario, asistencia);

        if (nuevoEstado == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar el nuevo estado de asistencia"
            );
        }

        asistencia.setEstado(nuevoEstado);

        return asistenciaRepository.save(asistencia);
    }

    @Transactional
    public void eliminar(
            Long asistenciaId,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        Asistencia asistencia = obtenerAsistencia(asistenciaId);

        validarPermiso(usuario, asistencia);

        asistenciaRepository.delete(asistencia);
    }
}
