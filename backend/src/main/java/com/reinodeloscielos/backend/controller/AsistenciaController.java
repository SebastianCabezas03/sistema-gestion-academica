package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.AsistenciaResponse;
import com.reinodeloscielos.backend.entity.*;
import com.reinodeloscielos.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final MatriculaRepository matriculas;
    private final CursoRepository cursoRepository;

    public AsistenciaController(
            AsistenciaRepository asistenciaRepository,
            UsuarioRepository usuarioRepository,
            AlumnoRepository alumnoRepository,
            CursoRepository cursoRepository, MatriculaRepository matriculas) {
        this.matriculas = matriculas;
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private AsistenciaResponse convertir(Asistencia asistencia) {
        Alumno alumno = asistencia.getAlumno();
        Curso curso = asistencia.getCurso();

        return new AsistenciaResponse(
                asistencia.getId(),
                alumno.getId(),
                alumno.getNombres() + " " + alumno.getApellidos(),
                curso.getId(),
                curso.getNombreCurso(),
                asistencia.getFecha(),
                asistencia.getEstado().name()
        );
    }

    // ALUMNO: consulta únicamente su propio historial.
    @GetMapping("/mi-asistencia")
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> miAsistencia(
        Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (!"ALUMNO".equals(usuario.getRol()) || usuario.getAlumno() == null) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Acceso exclusivo para alumnos");
        }

        return asistenciaRepository
                .findByAlumnoIdOrderByFechaDesc(usuario.getAlumno().getId())
                .stream()
                .map(this::convertir)
                .toList();
    }

    @GetMapping("/alumno/{alumnoId}")
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> asistenciaAlumno(
        @PathVariable Long alumnoId,
        Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (!"ADMIN".equals(usuario.getRol())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Acceso exclusivo para administradores");
        }

        return asistenciaRepository
                .findByAlumnoIdOrderByFechaDesc(alumnoId)
                .stream()
                .map(this::convertir)
                .toList();
        }

        @GetMapping("/admin/todas")
        @Transactional(readOnly = true)
        public List<AsistenciaResponse> todasLasAsistencias(
        Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (!"ADMIN".equals(usuario.getRol())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Acceso exclusivo para administradores"
        );
        }

        return asistenciaRepository.findAllByOrderByFechaDesc()
        .stream()
        .map(this::convertir)
        .toList();
}

    // DOCENTE: registra asistencia de sus propios cursos.
    @PostMapping("/registrar")
    @Transactional
    public AsistenciaResponse registrar(
        @jakarta.validation.Valid @RequestBody RegistrarAsistenciaRequest request,
        Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (!"DOCENTE".equals(usuario.getRol()) || usuario.getDocente() == null) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Acceso exclusivo para docentes");
        }

        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Curso no encontrado"));

        if (curso.getDocente() == null ||
                !curso.getDocente().getId().equals(usuario.getDocente().getId())) {
                throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "No tienes asignado este curso");
        }

        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Alumno no encontrado"));

        if (!matriculas.existsByAlumnoIdAndCursoIdAndEstado(alumno.getId(), curso.getId(), Matricula.Estado.ACTIVA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El alumno requiere matrícula activa en este curso.");
        }

        if (!asistenciaRepository.existsByAlumnoIdAndCursoIdAndFecha(
                alumno.getId(), curso.getId(), request.fecha())) {

        Asistencia asistencia = new Asistencia();
        asistencia.setAlumno(alumno);
        asistencia.setCurso(curso);
        asistencia.setFecha(request.fecha());
        asistencia.setEstado(request.estado());

        return convertir(asistenciaRepository.save(asistencia));
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe una asistencia para este alumno, curso y fecha");
        }

    public record RegistrarAsistenciaRequest(
        @jakarta.validation.constraints.NotNull Long alumnoId,
        @jakarta.validation.constraints.NotNull Long cursoId,
        @jakarta.validation.constraints.NotNull LocalDate fecha,
        @jakarta.validation.constraints.NotNull Asistencia.Estado estado
    ) {}
}