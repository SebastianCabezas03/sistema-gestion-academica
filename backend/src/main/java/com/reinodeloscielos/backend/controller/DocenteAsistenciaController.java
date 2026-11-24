package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.MatriculaResponseDto;
import com.reinodeloscielos.backend.entity.*;
import com.reinodeloscielos.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
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

    public record CursoAsignado(Long id, String nombreCurso, String nombreDocente) {}
    @GetMapping("/mis-cursos")
    @Transactional(readOnly = true)
    public List<CursoAsignado> misCursos(Authentication authentication) {

        Usuario usuario = obtenerDocente(authentication);

        return cursoRepository.findByDocenteIdOrderByNombreCursoAsc(
                usuario.getDocente().getId()
        ).stream().map(c -> new CursoAsignado(c.getId(), c.getNombreCurso(), usuario.getDocente().getNombres() + " " + usuario.getDocente().getApellidos())).toList();
    }

    @GetMapping("/curso/{cursoId}/alumnos")
    @Transactional(readOnly = true)
    public List<MatriculaResponseDto> alumnosDelCurso(
            @PathVariable Long cursoId,
            Authentication authentication) {

        Usuario usuario = obtenerDocente(authentication);

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Curso no encontrado"));

        if (curso.getDocente() == null ||
                !curso.getDocente().getId().equals(usuario.getDocente().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes asignado este curso");
        }

        return matriculaRepository
                .findByCursoIdAndEstadoOrderByAlumnoApellidosAsc(
                        cursoId,
                        Matricula.Estado.ACTIVA
                )
                .stream()
                .map(m -> new MatriculaResponseDto(
                        m.getId(),
                        m.getAlumno().getId(),
                        m.getAlumno().getNombres() + " " + m.getAlumno().getApellidos(),
                        m.getCurso().getId(),
                        m.getCurso().getNombreCurso(),
                        m.getFecha(),
                        m.getEstado()
                ))
                .toList();
    }

    private Usuario obtenerDocente(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }

        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (!"DOCENTE".equals(usuario.getRol()) || usuario.getDocente() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Acceso exclusivo para docentes");
        }

        return usuario;
    }
}