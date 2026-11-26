
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.CalificacionNotaRequest;
import com.reinodeloscielos.backend.dto.CalificacionRequest;
import com.reinodeloscielos.backend.dto.CalificacionResponse;

import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.entity.Calificacion;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Evaluacion;
import com.reinodeloscielos.backend.entity.Matricula;
import com.reinodeloscielos.backend.entity.Usuario;

import com.reinodeloscielos.backend.repository.AlumnoRepository;
import com.reinodeloscielos.backend.repository.CalificacionRepository;
import com.reinodeloscielos.backend.repository.EvaluacionRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final AlumnoRepository alumnoRepository;
    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;

    public CalificacionService(
            CalificacionRepository calificacionRepository,
            EvaluacionRepository evaluacionRepository,
            AlumnoRepository alumnoRepository,
            MatriculaRepository matriculaRepository,
            UsuarioRepository usuarioRepository) {

        this.calificacionRepository = calificacionRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.alumnoRepository = alumnoRepository;
        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Registrar una calificación
    @Transactional
    public CalificacionResponse registrar(
            CalificacionRequest request,
            Authentication authentication) {

        Evaluacion evaluacion = obtenerEvaluacion(request.evaluacionId());

        Curso curso = evaluacion.getCurso();

        validarAccesoCurso(curso, authentication);

        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno no encontrado"
                ));

        // Verificar que el alumno tenga matrícula activa
        boolean matriculaActiva =
                matriculaRepository.existsByAlumnoIdAndCursoIdAndEstado(
                        alumno.getId(),
                        curso.getId(),
                        Matricula.Estado.ACTIVA
                );

        if (!matriculaActiva) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El alumno no tiene una matrícula activa en este curso"
            );
        }

        // Evitar registrar dos notas para la misma evaluación y alumno
        if (calificacionRepository.existsByAlumnoIdAndEvaluacionId(
                alumno.getId(),
                evaluacion.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El alumno ya tiene una calificación en esta evaluación"
            );
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setAlumno(alumno);
        calificacion.setEvaluacion(evaluacion);
        calificacion.setNota(request.nota());

        return convertirAResponse(calificacionRepository.save(calificacion));
    }

    // Listar las calificaciones de una evaluación
    @Transactional(readOnly = true)
    public List<CalificacionResponse> listarPorEvaluacion(
            Long evaluacionId,
            Authentication authentication) {

        Evaluacion evaluacion = obtenerEvaluacion(evaluacionId);

        validarAccesoCurso(evaluacion.getCurso(), authentication);

        return calificacionRepository
                .findByEvaluacionIdOrderByAlumnoApellidosAsc(evaluacionId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Actualizar una calificación
    @Transactional
    public CalificacionResponse actualizar(
            Long calificacionId,
            CalificacionNotaRequest request,
            Authentication authentication) {

        Calificacion calificacion = calificacionRepository
                .findById(calificacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Calificación no encontrada"
                ));

        validarAccesoCurso(
                calificacion.getEvaluacion().getCurso(),
                authentication
        );

        calificacion.setNota(request.nota());

        return convertirAResponse(calificacionRepository.save(calificacion));
    }

    // Eliminar una calificación
    @Transactional
    public void eliminar(
            Long calificacionId,
            Authentication authentication) {

        Calificacion calificacion = calificacionRepository
                .findById(calificacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Calificación no encontrada"
                ));

        validarAccesoCurso(
                calificacion.getEvaluacion().getCurso(),
                authentication
        );

        calificacionRepository.delete(calificacion);
    }

    // Consultar las calificaciones del alumno autenticado
    @Transactional(readOnly = true)
    public List<CalificacionResponse> listarMisCalificaciones(
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (!"ALUMNO".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Esta consulta está disponible únicamente para alumnos"
            );
        }

        if (usuario.getAlumno() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El usuario no tiene un alumno asociado"
            );
        }

        return calificacionRepository
                .findByAlumnoIdOrderByEvaluacionFechaDesc(
                        usuario.getAlumno().getId()
                )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Obtener una evaluación
    private Evaluacion obtenerEvaluacion(Long evaluacionId) {

        return evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Evaluación no encontrada"
                ));
    }

    // Obtener usuario autenticado
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

    // Validar permisos sobre el curso
    private void validarAccesoCurso(
            Curso curso,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        String rol = usuario.getRol();

        // ADMIN puede gestionar cualquier curso
        if ("ADMIN".equals(rol)) {
            return;
        }

        // DOCENTE solamente puede gestionar sus cursos
        if ("DOCENTE".equals(rol)) {

            if (usuario.getDocente() == null
                    || curso.getDocente() == null
                    || !usuario.getDocente().getId()
                            .equals(curso.getDocente().getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes permiso para gestionar este curso"
                );
            }

            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para gestionar calificaciones"
        );
    }

    // Convertir entidad a DTO
    private CalificacionResponse convertirAResponse(
            Calificacion calificacion) {

        return new CalificacionResponse(
                calificacion.getId(),
                calificacion.getAlumno().getId(),
                calificacion.getAlumno().getNombres() + " "
                        + calificacion.getAlumno().getApellidos(),
                calificacion.getEvaluacion().getId(),
                calificacion.getEvaluacion().getNombre(),
                calificacion.getEvaluacion().getCurso().getNombreCurso(),
                calificacion.getEvaluacion().getFecha(),
                calificacion.getNota()
        );
    }
}
