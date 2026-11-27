
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.ResumenAsistenciaResponse;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.AsistenciaRepository;
import com.reinodeloscielos.backend.repository.AlumnoRepository;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ReporteAsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteAsistenciaService(
            AsistenciaRepository asistenciaRepository,
            AlumnoRepository alumnoRepository,
            CursoRepository cursoRepository,
            UsuarioRepository usuarioRepository) {

        this.asistenciaRepository = asistenciaRepository;
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public ResumenAsistenciaResponse obtenerResumen(
            Long alumnoId,
            Long cursoId,
            Authentication authentication) {

        // Verificar autenticación.
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

        // Validar los identificadores recibidos.
        if (alumnoId == null || alumnoId <= 0
                || cursoId == null || cursoId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los identificadores del alumno y del curso deben ser válidos"
            );
        }

        // Obtener el usuario autenticado.
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        String rol = usuario.getRol() == null
                ? ""
                : usuario.getRol().trim().toUpperCase(Locale.ROOT);

        // Solo ADMIN y DOCENTE pueden consultar este reporte.
        if (!"ADMIN".equals(rol) && !"DOCENTE".equals(rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar este reporte"
            );
        }

        // Buscar al alumno.
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno no encontrado"
                ));

        // Buscar el curso.
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        // Verificar que el docente tenga asignado el curso.
        if ("DOCENTE".equals(rol)) {

            if (usuario.getDocente() == null
                    || curso.getDocente() == null
                    || !Objects.equals(
                            curso.getDocente().getId(),
                            usuario.getDocente().getId()
                    )) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes asignado este curso"
                );
            }
        }

        // Obtener los registros de asistencia del alumno en el curso.
        List<Asistencia> registros = asistenciaRepository
                .findByAlumnoIdAndCursoIdOrderByFechaDesc(
                        alumnoId,
                        cursoId
                );

        // Calcular el total de registros.
        long total = registros.size();

        // Contar asistencias.
        long asistencias = registros.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.ASISTIO)
                .count();

        // Contar faltas.
        long faltas = registros.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.FALTA)
                .count();

        // Contar tardanzas.
        long tardanzas = registros.stream()
                .filter(a -> a.getEstado() == Asistencia.Estado.TARDANZA)
                .count();

        // Las tardanzas también cuentan como asistencia.
        double porcentaje = total == 0
                ? 0.0
                : Math.round(
                        ((asistencias + tardanzas) * 100.0 / total) * 100.0
                ) / 100.0;

        // Construir y devolver el resumen.
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
