
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.HistorialAcademicoResponse;
import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.entity.Matricula;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.AsistenciaRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HistorialAcademicoService {

    private final UsuarioRepository usuarioRepository;
    private final MatriculaRepository matriculaRepository;
    private final AsistenciaRepository asistenciaRepository;

    public HistorialAcademicoService(
            UsuarioRepository usuarioRepository,
            MatriculaRepository matriculaRepository,
            AsistenciaRepository asistenciaRepository) {

        this.usuarioRepository = usuarioRepository;
        this.matriculaRepository = matriculaRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    @Transactional(readOnly = true)
    public HistorialAcademicoResponse obtenerMiHistorial(
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

        if (!"ALUMNO".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Esta consulta está disponible únicamente para alumnos"
            );
        }

        Alumno alumno = usuario.getAlumno();

        if (alumno == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El usuario no tiene un alumno asociado"
            );
        }

        Long alumnoId = alumno.getId();

        // Obtener todas las matrículas del alumno
        List<Matricula> matriculas =
                matriculaRepository.findByAlumnoIdOrderByIdDesc(alumnoId);

        // Obtener todas las asistencias del alumno
        List<Asistencia> asistencias =
                asistenciaRepository.findByAlumnoIdOrderByFechaDesc(alumnoId);

        // Convertir matrículas al DTO
        List<HistorialAcademicoResponse.DetalleMatricula> detalleMatriculas =
                matriculas.stream()
                        .map(matricula ->
                                new HistorialAcademicoResponse.DetalleMatricula(
                                        matricula.getId(),
                                        matricula.getCurso().getNombreCurso(),
                                        matricula.getFecha(),
                                        matricula.getEstado().name()
                                ))
                        .toList();

        // Convertir asistencias al DTO
        List<HistorialAcademicoResponse.DetalleAsistencia> detalleAsistencias =
                asistencias.stream()
                        .map(asistencia ->
                                new HistorialAcademicoResponse.DetalleAsistencia(
                                        asistencia.getId(),
                                        asistencia.getCurso().getNombreCurso(),
                                        asistencia.getFecha(),
                                        asistencia.getEstado().name()
                                ))
                        .toList();

        // Calcular matrículas
        int matriculasActivas = (int) matriculas.stream()
                .filter(m -> m.getEstado() == Matricula.Estado.ACTIVA)
                .count();

        int matriculasCanceladas = (int) matriculas.stream()
                .filter(m -> m.getEstado() == Matricula.Estado.CANCELADA)
                .count();

        // Calcular asistencias
        int totalAsistencias = asistencias.size();

        int asistio = (int) asistencias.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.ASISTIO)
                .count();

        int faltas = (int) asistencias.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.FALTA)
                .count();

        int tardanzas = (int) asistencias.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.TARDANZA)
                .count();

        // Las tardanzas también cuentan como asistencia
        double porcentajeAsistencia = totalAsistencias == 0
                ? 0.0
                : ((double) (asistio + tardanzas) / totalAsistencias) * 100;

        HistorialAcademicoResponse.DatosAlumno datosAlumno =
                new HistorialAcademicoResponse.DatosAlumno(
                        alumno.getId(),
                        alumno.getNombres(),
                        alumno.getApellidos(),
                        alumno.getCorreo(),
                        alumno.getTelefono()
                );

        HistorialAcademicoResponse.ResumenAcademico resumen =
                new HistorialAcademicoResponse.ResumenAcademico(
                        matriculas.size(),
                        matriculasActivas,
                        matriculasCanceladas,
                        totalAsistencias,
                        asistio,
                        faltas,
                        tardanzas,
                        Math.round(porcentajeAsistencia * 100.0) / 100.0
                );

        return new HistorialAcademicoResponse(
                datosAlumno,
                resumen,
                detalleMatriculas,
                detalleAsistencias
        );
    }
}
