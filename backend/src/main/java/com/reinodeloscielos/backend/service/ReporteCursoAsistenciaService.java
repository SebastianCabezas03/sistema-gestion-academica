
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.ReporteCursoAsistenciaResponse;
import com.reinodeloscielos.backend.dto.ReporteCursoPeriodoResponse;
import com.reinodeloscielos.backend.dto.ResumenAsistenciaResponse;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Matricula;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.AsistenciaRepository;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReporteCursoAsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteCursoAsistenciaService(
            AsistenciaRepository asistenciaRepository,
            CursoRepository cursoRepository,
            MatriculaRepository matriculaRepository,
            UsuarioRepository usuarioRepository) {

        this.asistenciaRepository = asistenciaRepository;
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public ReporteCursoAsistenciaResponse obtenerReporte(
            Long cursoId,
            Authentication authentication) {

        Usuario usuario = validarUsuario(authentication);
        Curso curso = obtenerCursoAutorizado(cursoId, usuario);

        List<Matricula> matriculas =
                matriculaRepository.findByCursoIdAndEstadoOrderByAlumnoApellidosAsc(
                        cursoId,
                        Matricula.Estado.ACTIVA
                );

        List<ResumenAsistenciaResponse> reportes = matriculas.stream()
                .map(matricula -> generarResumen(
                        matricula,
                        curso,
                        asistenciaRepository.findByAlumnoIdAndCursoIdOrderByFechaDesc(
                                matricula.getAlumno().getId(),
                                cursoId
                        )
                ))
                .toList();

        return new ReporteCursoAsistenciaResponse(
                curso.getId(),
                curso.getNombreCurso(),
                reportes.size(),
                reportes
        );
    }

    @Transactional(readOnly = true)
    public ReporteCursoPeriodoResponse obtenerReportePorPeriodo(
            Long cursoId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Authentication authentication) {

        Usuario usuario = validarUsuario(authentication);

        if (fechaInicio == null || fechaFin == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar ambas fechas"
            );
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser posterior a la fecha final"
            );
        }

        Curso curso = obtenerCursoAutorizado(cursoId, usuario);

        List<Matricula> matriculas =
                matriculaRepository.findByCursoIdAndEstadoOrderByAlumnoApellidosAsc(
                        cursoId,
                        Matricula.Estado.ACTIVA
                );

        List<Asistencia> registrosPeriodo =
                asistenciaRepository.findByCursoIdAndFechaBetweenOrderByFechaDesc(
                        cursoId,
                        fechaInicio,
                        fechaFin
                );

        List<ResumenAsistenciaResponse> reportes = matriculas.stream()
                .map(matricula -> {

                    Long alumnoId = matricula.getAlumno().getId();

                    List<Asistencia> registrosAlumno = registrosPeriodo.stream()
                            .filter(asistencia ->
                                    asistencia.getAlumno().getId().equals(alumnoId))
                            .toList();

                    return generarResumen(
                            matricula,
                            curso,
                            registrosAlumno
                    );
                })
                .toList();

        return new ReporteCursoPeriodoResponse(
                curso.getId(),
                curso.getNombreCurso(),
                fechaInicio,
                fechaFin,
                reportes.size(),
                reportes
        );
    }

    private Usuario validarUsuario(Authentication authentication) {

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

        return usuario;
    }

    private Curso obtenerCursoAutorizado(
            Long cursoId,
            Usuario usuario) {

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

        return curso;
    }

    private ResumenAsistenciaResponse generarResumen(
            Matricula matricula,
            Curso curso,
            List<Asistencia> registros) {

        var alumno = matricula.getAlumno();

        long total = registros.size();

        long asistencias = registros.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.ASISTIO)
                .count();

        long faltas = registros.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.FALTA)
                .count();

        long tardanzas = registros.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.TARDANZA)
                .count();

        double porcentaje = total == 0
                ? 0.0
                : Math.round(
                        ((asistencias + tardanzas) * 100.0 / total) * 100.0
                ) / 100.0;

        return new ResumenAsistenciaResponse(
                alumno.getId(),
                alumno.getNombres() + " " + alumno.getApellidos(),
                curso.getId(),
                curso.getNombreCurso(),
                total,
                asistencias,
                faltas,
                tardanzas,
                porcentaje
        );
    }
}

