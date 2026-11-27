
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.MatriculaResponseDto;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Matricula;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;

import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/docente/asistencia")
public class DocenteAsistenciaController {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;

    public DocenteAsistenciaController(
            UsuarioRepository usuarioRepository,
            CursoRepository cursoRepository,
            MatriculaRepository matriculaRepository) {

        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    // Obtener los cursos asignados al docente autenticado.
    @GetMapping("/mis-cursos")
    @Transactional(readOnly = true)
    public List<Curso> misCursos(Authentication authentication) {

        Usuario usuario = obtenerDocente(authentication);

        return cursoRepository.findByDocenteIdOrderByNombreCursoAsc(
                usuario.getDocente().getId()
        );
    }

    // Obtener los alumnos con matrícula activa en un curso.
    @GetMapping("/curso/{cursoId}/alumnos")
    @Transactional(readOnly = true)
    public List<MatriculaResponseDto> alumnosDelCurso(
            @PathVariable
            @Positive(message = "El ID del curso debe ser positivo")
            Long cursoId,

            Authentication authentication) {

        Usuario usuario = obtenerDocente(authentication);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        // Verificar que el curso pertenezca al docente autenticado.
        if (curso.getDocente() == null
                || !curso.getDocente().getId()
                        .equals(usuario.getDocente().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes asignado este curso"
            );
        }

        // Recuperar únicamente las matrículas activas.
        return matriculaRepository
                .findByCursoIdAndEstadoOrderByAlumnoApellidosAsc(
                        cursoId,
                        Matricula.Estado.ACTIVA
                )
                .stream()
                .map(matricula -> new MatriculaResponseDto(
                        matricula.getId(),
                        matricula.getAlumno().getId(),
                        matricula.getAlumno().getNombres() + " "
                                + matricula.getAlumno().getApellidos(),
                        matricula.getCurso().getId(),
                        matricula.getCurso().getNombreCurso(),
                        matricula.getFecha(),
                        matricula.getEstado()
                ))
                .toList();
    }

    // Validar que el usuario esté autenticado y tenga rol DOCENTE.
    private Usuario obtenerDocente(Authentication authentication) {

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

        if (!"DOCENTE".equals(usuario.getRol())
                || usuario.getDocente() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso exclusivo para docentes"
            );
        }

        return usuario;
    }
}
