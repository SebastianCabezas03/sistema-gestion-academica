
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.RendimientoAcademicoResponse;

import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.entity.Calificacion;
import com.reinodeloscielos.backend.entity.Usuario;

import com.reinodeloscielos.backend.repository.AsistenciaRepository;
import com.reinodeloscielos.backend.repository.CalificacionRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RendimientoAcademicoService {

    private final CalificacionRepository calificacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public RendimientoAcademicoService(
            CalificacionRepository calificacionRepository,
            AsistenciaRepository asistenciaRepository,
            UsuarioRepository usuarioRepository) {

        this.calificacionRepository = calificacionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public RendimientoAcademicoResponse obtenerRendimiento(
            Authentication authentication) {

        validarAdministrador(authentication);

        // Obtener todas las calificaciones
        List<Calificacion> calificaciones =
                calificacionRepository.findAll();

        long totalCalificaciones = calificaciones.size();

        // Calcular promedio general
        BigDecimal promedioGeneral = BigDecimal.ZERO;

        if (!calificaciones.isEmpty()) {

            BigDecimal sumaNotas = calificaciones.stream()
                    .map(Calificacion::getNota)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            promedioGeneral = sumaNotas.divide(
                    BigDecimal.valueOf(totalCalificaciones),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        // Obtener estadísticas de asistencia
        long totalAsistencias =
                asistenciaRepository.countByEstado(
                        Asistencia.Estado.ASISTIO
                );

        long totalFaltas =
                asistenciaRepository.countByEstado(
                        Asistencia.Estado.FALTA
                );

        long totalTardanzas =
                asistenciaRepository.countByEstado(
                        Asistencia.Estado.TARDANZA
                );

        long totalRegistrosAsistencia =
                totalAsistencias + totalFaltas + totalTardanzas;

        // Calcular porcentaje de asistencia
        // Las tardanzas también cuentan como asistencia
        BigDecimal porcentajeAsistencia = BigDecimal.ZERO;

        if (totalRegistrosAsistencia > 0) {

            BigDecimal asistenciasEfectivas =
                    BigDecimal.valueOf(totalAsistencias + totalTardanzas);

            porcentajeAsistencia = asistenciasEfectivas
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            BigDecimal.valueOf(totalRegistrosAsistencia),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return new RendimientoAcademicoResponse(
                totalCalificaciones,
                promedioGeneral,
                totalAsistencias,
                totalFaltas,
                totalTardanzas,
                totalRegistrosAsistencia,
                porcentajeAsistencia
        );
    }

    // Validar acceso exclusivo del administrador
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
                    "No tienes permisos para consultar el rendimiento académico"
            );
        }
    }
}
