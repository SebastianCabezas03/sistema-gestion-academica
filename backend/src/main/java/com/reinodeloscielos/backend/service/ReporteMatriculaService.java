
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.ReporteMatriculaResponse;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Matricula;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReporteMatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteMatriculaService(
            MatriculaRepository matriculaRepository,
            CursoRepository cursoRepository,
            UsuarioRepository usuarioRepository) {

        this.matriculaRepository = matriculaRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public ReporteMatriculaResponse obtenerReporte(
            Long cursoId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        if (!"ADMIN".equals(usuario.getRol())
                && !"DOCENTE".equals(usuario.getRol())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar este reporte"
            );
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        if ("DOCENTE".equals(usuario.getRol())) {

            if (usuario.getDocente() == null
                    || curso.getDocente() == null
                    || !curso.getDocente().getId()
                            .equals(usuario.getDocente().getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes asignado este curso"
                );
            }
        }

        List<Matricula> matriculas =
                matriculaRepository.findByCursoIdOrderByIdDesc(cursoId);

        List<ReporteMatriculaResponse.DetalleMatricula> detalles =
                matriculas.stream()
                        .map(matricula ->
                                new ReporteMatriculaResponse.DetalleMatricula(
                                        matricula.getId(),
                                        matricula.getAlumno().getId(),
                                        matricula.getAlumno().getNombres()
                                                + " "
                                                + matricula.getAlumno().getApellidos(),
                                        matricula.getFecha(),
                                        matricula.getEstado().name()
                                )
                        )
                        .toList();

        int activas = (int) matriculas.stream()
                .filter(m -> m.getEstado() == Matricula.Estado.ACTIVA)
                .count();

        int canceladas = (int) matriculas.stream()
                .filter(m -> m.getEstado() == Matricula.Estado.CANCELADA)
                .count();

        return new ReporteMatriculaResponse(
                curso.getId(),
                curso.getNombreCurso(),
                matriculas.size(),
                activas,
                canceladas,
                detalles
        );
    }
}
