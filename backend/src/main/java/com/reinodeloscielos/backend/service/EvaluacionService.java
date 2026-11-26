
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.EvaluacionRequest;
import com.reinodeloscielos.backend.dto.EvaluacionResponse;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Evaluacion;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.CalificacionRepository;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.EvaluacionRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final CalificacionRepository calificacionRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    public EvaluacionService(
            EvaluacionRepository evaluacionRepository,
            CalificacionRepository calificacionRepository,
            CursoRepository cursoRepository,
            UsuarioRepository usuarioRepository) {

        this.evaluacionRepository = evaluacionRepository;
        this.calificacionRepository = calificacionRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Registrar una evaluación
    @Transactional
    public EvaluacionResponse crear(
            EvaluacionRequest request,
            Authentication authentication) {

        Curso curso = obtenerCurso(request.cursoId());

        validarAccesoCurso(curso, authentication);

        if (evaluacionRepository.existsByCursoIdAndNombreIgnoreCase(
                curso.getId(), request.nombre())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una evaluación con ese nombre en el curso"
            );
        }

        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setNombre(request.nombre());
        evaluacion.setDescripcion(request.descripcion());
        evaluacion.setFecha(request.fecha());
        evaluacion.setCurso(curso);

        return convertirAResponse(evaluacionRepository.save(evaluacion));
    }

    // Listar evaluaciones de un curso
    @Transactional(readOnly = true)
    public List<EvaluacionResponse> listarPorCurso(
            Long cursoId,
            Authentication authentication) {

        Curso curso = obtenerCurso(cursoId);

        validarAccesoCurso(curso, authentication);

        return evaluacionRepository
                .findByCursoIdOrderByFechaDesc(cursoId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Actualizar una evaluación
    @Transactional
    public EvaluacionResponse actualizar(
            Long evaluacionId,
            EvaluacionRequest request,
            Authentication authentication) {

        Evaluacion evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Evaluación no encontrada"
                ));

        // Validar acceso al curso actual
        validarAccesoCurso(evaluacion.getCurso(), authentication);

        // Obtener y validar el curso solicitado
        Curso curso = obtenerCurso(request.cursoId());

        validarAccesoCurso(curso, authentication);

        // Evitar nombres duplicados
        if (evaluacionRepository.existsByCursoIdAndNombreIgnoreCaseAndIdNot(
                curso.getId(),
                request.nombre(),
                evaluacionId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra evaluación con ese nombre en el curso"
            );
        }

        evaluacion.setNombre(request.nombre());
        evaluacion.setDescripcion(request.descripcion());
        evaluacion.setFecha(request.fecha());
        evaluacion.setCurso(curso);

        return convertirAResponse(evaluacionRepository.save(evaluacion));
    }

    // Eliminar una evaluación
    @Transactional
    public void eliminar(
            Long evaluacionId,
            Authentication authentication) {

        Evaluacion evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Evaluación no encontrada"
                ));

        validarAccesoCurso(evaluacion.getCurso(), authentication);

        // Verificar si existen calificaciones asociadas
        if (calificacionRepository.existsByEvaluacionId(evaluacionId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar una evaluación que ya tiene calificaciones"
            );
        }

        evaluacionRepository.delete(evaluacion);
    }

    // Buscar un curso por su ID
    private Curso obtenerCurso(Long cursoId) {

        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));
    }

    // Validar los permisos del usuario
    private void validarAccesoCurso(
            Curso curso,
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

        String rol = usuario.getRol();

        // El administrador puede gestionar cualquier curso
        if ("ADMIN".equals(rol)) {
            return;
        }

        // El docente solamente puede gestionar sus propios cursos
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
                "No tienes permisos para gestionar evaluaciones"
        );
    }

    // Convertir entidad a DTO
    private EvaluacionResponse convertirAResponse(Evaluacion evaluacion) {

        return new EvaluacionResponse(
                evaluacion.getId(),
                evaluacion.getNombre(),
                evaluacion.getDescripcion(),
                evaluacion.getFecha(),
                evaluacion.getCurso().getId(),
                evaluacion.getCurso().getNombreCurso()
        );
    }
}

