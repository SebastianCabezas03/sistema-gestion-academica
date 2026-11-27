
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.MatriculaRequestDto;
import com.reinodeloscielos.backend.dto.MatriculaResponseDto;
import com.reinodeloscielos.backend.dto.MatriculaUpdateDto;

import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Matricula;

import com.reinodeloscielos.backend.repository.AlumnoRepository;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.MatriculaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class MatriculaService {

    private final MatriculaRepository matriculas;
    private final AlumnoRepository alumnos;
    private final CursoRepository cursos;

    public MatriculaService(
            MatriculaRepository matriculas,
            AlumnoRepository alumnos,
            CursoRepository cursos) {

        this.matriculas = matriculas;
        this.alumnos = alumnos;
        this.cursos = cursos;
    }

    // Listar todas las matrículas.
    @Transactional(readOnly = true)
    public List<MatriculaResponseDto> listar() {

        return matriculas.findAllByOrderByIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Registrar uno o varios cursos para un alumno.
    public List<MatriculaResponseDto> registrar(MatriculaRequestDto dto) {

        if (dto == null
                || dto.alumnoId() == null
                || dto.alumnoId() <= 0
                || dto.cursosIds() == null
                || dto.cursosIds().isEmpty()
                || dto.fecha() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos de matrícula son obligatorios"
            );
        }

        Alumno alumno = alumno(dto.alumnoId());

        // Evitar cursos repetidos en la solicitud.
        Set<Long> ids = new LinkedHashSet<>(dto.cursosIds());

        if (ids.contains(null) || ids.stream().anyMatch(id -> id <= 0)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los IDs de los cursos deben ser válidos"
            );
        }

        if (ids.size() != dto.cursosIds().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La lista contiene cursos repetidos"
            );
        }

        // Verificar que todos los cursos existan.
        List<Curso> seleccionados = ids.stream()
                .map(this::curso)
                .toList();

        // Preparar las matrículas.
        List<Matricula> nuevas = seleccionados.stream()
                .map(curso -> {

                    Matricula existente = matriculas
                            .findByAlumnoIdAndCursoId(
                                    alumno.getId(),
                                    curso.getId()
                            )
                            .orElse(null);

                    if (existente != null) {

                        if (existente.getEstado() == Matricula.Estado.ACTIVA) {
                            throw duplicada();
                        }

                        // Reactivar matrícula cancelada.
                        existente.setEstado(Matricula.Estado.ACTIVA);
                        existente.setFecha(dto.fecha());

                        return existente;
                    }

                    Matricula matricula = new Matricula();
                    matricula.setAlumno(alumno);
                    matricula.setCurso(curso);
                    matricula.setFecha(dto.fecha());
                    matricula.setEstado(Matricula.Estado.ACTIVA);

                    return matricula;
                })
                .toList();

        return matriculas.saveAllAndFlush(nuevas)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Actualizar una matrícula existente.
    public MatriculaResponseDto actualizar(
            Long id,
            MatriculaUpdateDto dto) {

        if (id == null || id <= 0 || dto == null
                || dto.alumnoId() == null
                || dto.alumnoId() <= 0
                || dto.cursoId() == null
                || dto.cursoId() <= 0
                || dto.fecha() == null
                || dto.estado() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos de actualización no son válidos"
            );
        }

        Matricula matricula = buscar(id);

        Alumno alumno = alumno(dto.alumnoId());
        Curso curso = curso(dto.cursoId());

        // Evitar duplicados excluyendo la matrícula actual.
        if (matriculas.existsByAlumnoIdAndCursoIdAndIdNot(
                alumno.getId(),
                curso.getId(),
                id)) {

            throw duplicada();
        }

        matricula.setAlumno(alumno);
        matricula.setCurso(curso);
        matricula.setFecha(dto.fecha());
        matricula.setEstado(dto.estado());

        return toDto(matriculas.saveAndFlush(matricula));
    }

    // Cancelar una matrícula.
    public MatriculaResponseDto cancelar(Long id) {

        Matricula matricula = buscar(id);

        matricula.setEstado(Matricula.Estado.CANCELADA);

        return toDto(matriculas.saveAndFlush(matricula));
    }

    // Buscar una matrícula por su identificador.
    private Matricula buscar(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID de matrícula no es válido"
            );
        }

        return matriculas.findById(id)
                .orElseThrow(() -> noEncontrado("Matrícula"));
    }

    // Buscar un alumno por su identificador.
    private Alumno alumno(Long id) {

        return alumnos.findById(id)
                .orElseThrow(() -> noEncontrado("Alumno"));
    }

    // Buscar un curso por su identificador.
    private Curso curso(Long id) {

        return cursos.findById(id)
                .orElseThrow(() -> noEncontrado("Curso"));
    }

    // Excepción cuando un recurso no existe.
    private ResponseStatusException noEncontrado(String recurso) {

        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                recurso + " no encontrado/a"
        );
    }

    // Excepción cuando existe una matrícula duplicada.
    private ResponseStatusException duplicada() {

        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El alumno ya tiene una matrícula activa en este curso."
        );
    }

    // Convertir una entidad Matricula en su DTO de respuesta.
    private MatriculaResponseDto toDto(Matricula matricula) {

        return new MatriculaResponseDto(
                matricula.getId(),
                matricula.getAlumno().getId(),
                matricula.getAlumno().getNombres() + " "
                        + matricula.getAlumno().getApellidos(),
                matricula.getCurso().getId(),
                matricula.getCurso().getNombreCurso(),
                matricula.getFecha(),
                matricula.getEstado()
        );
    }
}

