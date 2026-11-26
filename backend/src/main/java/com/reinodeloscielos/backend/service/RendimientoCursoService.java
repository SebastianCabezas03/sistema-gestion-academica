
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.RendimientoCursoResponse;

import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.entity.Calificacion;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Usuario;

import com.reinodeloscielos.backend.repository.AsistenciaRepository;
import com.reinodeloscielos.backend.repository.CalificacionRepository;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RendimientoCursoService {

    private final CursoRepository cursoRepository;
    private final CalificacionRepository calificacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public RendimientoCursoService(
            CursoRepository cursoRepository,
            CalificacionRepository calificacionRepository,
            AsistenciaRepository asistenciaRepository,
            UsuarioRepository usuarioRepository) {

        this.cursoRepository = cursoRepository;
        this.calificacionRepository = calificacionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<RendimientoCursoResponse> obtenerRendimientoPorCurso(
            Authentication authentication) {

        validarAdministrador(authentication);

        List<Curso> cursos = cursoRepository.findAll();

        List<Calificacion> calificaciones =
                calificacionRepository.findAll();

        List<Asistencia> asistencias =
                asistenciaRepository.findAllByOrderByFechaDesc();

        // Crear un acumulador para cada curso
        Map<Long, AcumuladorCurso> acumuladores = new HashMap<>();

        for (Curso curso : cursos) {
            acumuladores.put(
                    curso.getId(),
                    new AcumuladorCurso()
            );
        }

        // Agrupar calificaciones por curso
        for (Calificacion calificacion : calificaciones) {

            Long cursoId = calificacion.getEvaluacion()
                    .getCurso()
                    .getId();

            AcumuladorCurso acumulador =
                    acumuladores.get(cursoId);

            if (acumulador != null) {
                acumulador.sumaNotas =
                        acumulador.sumaNotas.add(calificacion.getNota());

                acumulador.totalCalificaciones++;
            }
        }

        // Agrupar asistencias por curso
        for (Asistencia asistencia : asistencias) {

            Long cursoId = asistencia.getCurso().getId();

            AcumuladorCurso acumulador =
                    acumuladores.get(cursoId);

            if (acumulador != null) {

                acumulador.totalRegistrosAsistencia++;

                switch (asistencia.getEstado()) {

                    case ASISTIO ->
                            acumulador.totalAsistencias++;

                    case FALTA ->
                            acumulador.totalFaltas++;

                    case TARDANZA ->
                            acumulador.totalTardanzas++;
                }
            }
        }

        // Construir respuesta para cada curso
        List<RendimientoCursoResponse> resultados =
                new ArrayList<>();

        for (Curso curso : cursos) {

            AcumuladorCurso acumulador =
                    acumuladores.get(curso.getId());

            BigDecimal promedioGeneral = BigDecimal.ZERO;

            if (acumulador.totalCalificaciones > 0) {

                promedioGeneral = acumulador.sumaNotas.divide(
                        BigDecimal.valueOf(
                                acumulador.totalCalificaciones
                        ),
                        2,
                        RoundingMode.HALF_UP
                );
            }

            BigDecimal porcentajeAsistencia = BigDecimal.ZERO;

            if (acumulador.totalRegistrosAsistencia > 0) {

                BigDecimal asistenciasEfectivas =
                        BigDecimal.valueOf(
                                acumulador.totalAsistencias
                                        + acumulador.totalTardanzas
                        );

                porcentajeAsistencia = asistenciasEfectivas
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                BigDecimal.valueOf(
                                        acumulador.totalRegistrosAsistencia
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );
            }

            resultados.add(
                    new RendimientoCursoResponse(
                            curso.getId(),
                            curso.getNombreCurso(),
                            acumulador.totalCalificaciones,
                            promedioGeneral,
                            acumulador.totalAsistencias,
                            acumulador.totalFaltas,
                            acumulador.totalTardanzas,
                            acumulador.totalRegistrosAsistencia,
                            porcentajeAsistencia
                    )
            );
        }

        return resultados;
    }

    // Validar que solamente ADMIN pueda consultar las estadísticas
    private void validarAdministrador(
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

        if (!"ADMIN".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar el rendimiento académico"
            );
        }
    }

    // Clase auxiliar para acumular las estadísticas de cada curso
    private static class AcumuladorCurso {

        private BigDecimal sumaNotas = BigDecimal.ZERO;

        private long totalCalificaciones = 0;

        private long totalAsistencias = 0;

        private long totalFaltas = 0;

        private long totalTardanzas = 0;

        private long totalRegistrosAsistencia = 0;
    }
}
