
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

        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno no encontrado"
                ));

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

        List<Asistencia> registros = asistenciaRepository
                .findByAlumnoIdAndCursoIdOrderByFechaDesc(
                        alumnoId,
                        cursoId
                );

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

        // Las tardanzas también cuentan como asistencia.
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
