package com.reinodeloscielos.backend.service.impl;

import com.reinodeloscielos.backend.dto.AsistenciaLoteRequest;
import com.reinodeloscielos.backend.dto.AsistenciaResponse;
import com.reinodeloscielos.backend.entity.*;
import com.reinodeloscielos.backend.repository.*;
import com.reinodeloscielos.backend.service.AsistenciaService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;

    public AsistenciaServiceImpl(
            AsistenciaRepository asistenciaRepository,
            UsuarioRepository usuarioRepository,
            AlumnoRepository alumnoRepository,
            CursoRepository cursoRepository,
            MatriculaRepository matriculaRepository) {

        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
    }

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

    private void validarRol(Usuario usuario, String rol) {

        if (!rol.equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para realizar esta operación"
            );
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> miAsistencia(
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);
        validarRol(usuario, "ALUMNO");

        if (usuario.getAlumno() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene un alumno asociado"
            );
        }

        return asistenciaRepository
                .findByAlumnoIdOrderByFechaDesc(usuario.getAlumno().getId())
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> asistenciaAlumno(
            Long alumnoId,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);
        validarRol(usuario, "ADMIN");

        return asistenciaRepository
                .findByAlumnoIdOrderByFechaDesc(alumnoId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> todasLasAsistencias(
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);
        validarRol(usuario, "ADMIN");

        return asistenciaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> asistenciaCurso(
            Long cursoId,
            LocalDate fecha,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (!"ADMIN".equals(usuario.getRol())
                && !"DOCENTE".equals(usuario.getRol())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para consultar estas asistencias"
            );
        }

        if (fecha == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha es obligatoria"
            );
        }

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

        return asistenciaRepository
                .findByCursoIdAndFechaOrderByAlumnoApellidosAsc(
                        cursoId,
                        fecha
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional
    public AsistenciaResponse registrar(
            Long alumnoId,
            Long cursoId,
            LocalDate fecha,
            Asistencia.Estado estado,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);
        validarRol(usuario, "DOCENTE");

        if (usuario.getDocente() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene un docente asociado"
            );
        }

        if (alumnoId == null || cursoId == null
                || fecha == null || estado == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Todos los datos de asistencia son obligatorios"
            );
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        validarCursoDocente(curso, usuario);

        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno no encontrado"
                ));

        validarMatriculaActiva(alumnoId, cursoId);

        if (asistenciaRepository.existsByAlumnoIdAndCursoIdAndFecha(
                alumnoId, cursoId, fecha)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una asistencia para este alumno, curso y fecha"
            );
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setAlumno(alumno);
        asistencia.setCurso(curso);
        asistencia.setFecha(fecha);
        asistencia.setEstado(estado);

        return convertir(asistenciaRepository.save(asistencia));
    }

    @Override
    @Transactional
    public List<AsistenciaResponse> registrarLote(
            AsistenciaLoteRequest request,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);
        validarRol(usuario, "DOCENTE");

        if (usuario.getDocente() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene un docente asociado"
            );
        }

        if (request == null || request.cursoId() == null
                || request.fecha() == null
                || request.asistencias() == null
                || request.asistencias().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar el curso, la fecha y los alumnos"
            );
        }

        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        validarCursoDocente(curso, usuario);

        Set<Long> alumnosProcesados = new HashSet<>();
        List<Asistencia> nuevasAsistencias = new ArrayList<>();

        for (AsistenciaLoteRequest.Detalle detalle : request.asistencias()) {

            if (detalle == null || detalle.alumnoId() == null
                    || detalle.estado() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cada alumno debe tener un ID y un estado de asistencia"
                );
            }

            Long alumnoId = detalle.alumnoId();

            if (!alumnosProcesados.add(alumnoId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La lista contiene alumnos duplicados"
                );
            }

            Alumno alumno = alumnoRepository.findById(alumnoId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No se encontró el alumno con ID " + alumnoId
                    ));

            validarMatriculaActiva(alumnoId, request.cursoId());

            if (asistenciaRepository.existsByAlumnoIdAndCursoIdAndFecha(
                    alumnoId, request.cursoId(), request.fecha())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe una asistencia para el alumno con ID "
                                + alumnoId + " en esa fecha"
                );
            }

            Asistencia asistencia = new Asistencia();
            asistencia.setAlumno(alumno);
            asistencia.setCurso(curso);
            asistencia.setFecha(request.fecha());
            asistencia.setEstado(detalle.estado());

            nuevasAsistencias.add(asistencia);
        }

        return asistenciaRepository.saveAll(nuevasAsistencias)
                .stream()
                .map(this::convertir)
                .toList();
    }

    private void validarCursoDocente(Curso curso, Usuario usuario) {

        if (curso.getDocente() == null
                || !curso.getDocente().getId()
                        .equals(usuario.getDocente().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes asignado este curso"
            );
        }
    }

    private void validarMatriculaActiva(Long alumnoId, Long cursoId) {

        boolean matriculaActiva =
                matriculaRepository.existsByAlumnoIdAndCursoIdAndEstado(
                        alumnoId,
                        cursoId,
                        Matricula.Estado.ACTIVA
                );

        if (!matriculaActiva) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El alumno no tiene una matrícula activa en este curso"
            );
        }
    }
}
