
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
import java.util.Objects;

@Service
@Transactional(readOnly = true)
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

    // Obtener reporte de matrículas de un curso.
    public ReporteMatriculaResponse obtenerReporte(
            Long cursoId,
            Authentication authentication) {

        // Validar identificador.
        if (cursoId == null || cursoId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del curso no es válido"
            );
        }

        // Verificar autenticación.
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

        // Obtener usuario autenticado.
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        // Verificar permisos.
        boolean esAdmin = "ADMIN".equals(usuario.getRol());
        boolean esDocente = "DOCENTE".equals(usuario.getRol());

        if (!esAdmin && !esDocente) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar este reporte"
            );
        }

        // Buscar curso.
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        // Verificar que el docente tenga asignado el curso.
        if (esDocente) {

            if (usuario.getDocente() == null
                    || curso.getDocente() == null
                    || !Objects.equals(
                            curso.getDocente().getId(),
                            usuario.getDocente().getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes asignado este curso"
                );
            }
        }

        // Obtener todas las matrículas del curso.
        List<Matricula> matriculas =
                matriculaRepository.findByCursoIdOrderByIdDesc(cursoId);

        // Construir detalles del reporte.
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

        // Calcular estadísticas.
        int activas = (int) matriculas.stream()
                .filter(m -> m.getEstado() == Matricula.Estado.ACTIVA)
                .count();

        int canceladas = (int) matriculas.stream()
                .filter(m -> m.getEstado() == Matricula.Estado.CANCELADA)
                .count();

        // Retornar reporte.
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

