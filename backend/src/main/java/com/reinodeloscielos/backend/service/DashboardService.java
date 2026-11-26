
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.DashboardResponse;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.entity.Matricula;
import com.reinodeloscielos.backend.entity.Usuario;

import com.reinodeloscielos.backend.repository.AlumnoRepository;
import com.reinodeloscielos.backend.repository.AsistenciaRepository;
import com.reinodeloscielos.backend.repository.CalificacionRepository;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.DocenteRepository;
import com.reinodeloscielos.backend.repository.EvaluacionRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {

    private final AlumnoRepository alumnoRepository;
    private final DocenteRepository docenteRepository;
    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final CalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardService(
            AlumnoRepository alumnoRepository,
            DocenteRepository docenteRepository,
            CursoRepository cursoRepository,
            MatriculaRepository matriculaRepository,
            AsistenciaRepository asistenciaRepository,
            EvaluacionRepository evaluacionRepository,
            CalificacionRepository calificacionRepository,
            UsuarioRepository usuarioRepository) {

        this.alumnoRepository = alumnoRepository;
        this.docenteRepository = docenteRepository;
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.calificacionRepository = calificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener estadísticas generales del sistema
    @Transactional(readOnly = true)
    public DashboardResponse obtenerEstadisticas(
            Authentication authentication) {

        validarAdministrador(authentication);

        Long totalAlumnos = alumnoRepository.count();

        Long totalDocentes = docenteRepository.count();

        Long totalCursos = cursoRepository.count();

        Long totalMatriculas = matriculaRepository.count();

        Long matriculasActivas =
                matriculaRepository.countByEstado(
                        Matricula.Estado.ACTIVA
                );

        Long matriculasCanceladas =
                matriculaRepository.countByEstado(
                        Matricula.Estado.CANCELADA
                );

        Long totalAsistencias = asistenciaRepository.count();

        Long totalEvaluaciones = evaluacionRepository.count();

        Long totalCalificaciones = calificacionRepository.count();

        return new DashboardResponse(
                totalAlumnos,
                totalDocentes,
                totalCursos,
                totalMatriculas,
                matriculasActivas,
                matriculasCanceladas,
                totalAsistencias,
                totalEvaluaciones,
                totalCalificaciones
        );
    }

    // Validar que solamente ADMIN pueda consultar el dashboard
    private void validarAdministrador(Authentication authentication) {

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

        if (!"ADMIN".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar el dashboard"
            );
        }
    }
}
