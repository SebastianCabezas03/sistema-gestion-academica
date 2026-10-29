package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.*;
import com.reinodeloscielos.backend.entity.*;
import com.reinodeloscielos.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@Transactional
public class MatriculaService {
    private final MatriculaRepository matriculas;
    private final AlumnoRepository alumnos;
    private final CursoRepository cursos;

    public MatriculaService(MatriculaRepository matriculas, AlumnoRepository alumnos, CursoRepository cursos) {
        this.matriculas = matriculas;
        this.alumnos = alumnos;
        this.cursos = cursos;
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponseDto> listar() {
        return matriculas.findAllByOrderByIdDesc().stream().map(this::toDto).toList();
    }

    public List<MatriculaResponseDto> registrar(MatriculaRequestDto dto) {
        Alumno alumno = alumno(dto.alumnoId());
        Set<Long> ids = new LinkedHashSet<>(dto.cursosIds());
        if (ids.size() != dto.cursosIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La lista contiene cursos repetidos");
        }
        // Validar toda la selección antes de guardar; el lote es una sola transacción.
        List<Curso> seleccionados = ids.stream().map(this::curso).toList();
        for (Curso curso : seleccionados) {
            if (matriculas.existsByAlumnoIdAndCursoId(alumno.getId(), curso.getId())) {
                throw duplicada();
            }
        }
        List<Matricula> nuevas = seleccionados.stream().map(curso -> {
            Matricula matricula = new Matricula();
            matricula.setAlumno(alumno);
            matricula.setCurso(curso);
            matricula.setFecha(dto.fecha());
            return matricula;
        }).toList();
        return matriculas.saveAllAndFlush(nuevas).stream().map(this::toDto).toList();
    }

    public MatriculaResponseDto actualizar(Long id, MatriculaUpdateDto dto) {
        Matricula matricula = buscar(id);
        Alumno alumno = alumno(dto.alumnoId());
        Curso curso = curso(dto.cursoId());
        if (matriculas.existsByAlumnoIdAndCursoIdAndIdNot(dto.alumnoId(), dto.cursoId(), id)) {
            throw duplicada();
        }
        matricula.setAlumno(alumno);
        matricula.setCurso(curso);
        matricula.setFecha(dto.fecha());
        matricula.setEstado(dto.estado());
        return toDto(matriculas.saveAndFlush(matricula));
    }

    public MatriculaResponseDto cancelar(Long id) {
        Matricula matricula = buscar(id);
        matricula.setEstado(Matricula.Estado.CANCELADA);
        return toDto(matriculas.saveAndFlush(matricula));
    }

    private Matricula buscar(Long id) {
        return matriculas.findById(id).orElseThrow(() -> noEncontrado("Matrícula"));
    }
    private Alumno alumno(Long id) {
        return alumnos.findById(id).orElseThrow(() -> noEncontrado("Alumno"));
    }
    private Curso curso(Long id) {
        return cursos.findById(id).orElseThrow(() -> noEncontrado("Curso"));
    }
    private ResponseStatusException noEncontrado(String recurso) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " no encontrado/a");
    }
    private ResponseStatusException duplicada() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "El alumno ya tiene una matrícula en uno de los cursos. Si está cancelada, edítala para reactivarla.");
    }
    private MatriculaResponseDto toDto(Matricula m) {
        return new MatriculaResponseDto(m.getId(), m.getAlumno().getId(),
                m.getAlumno().getNombres() + " " + m.getAlumno().getApellidos(),
                m.getCurso().getId(), m.getCurso().getNombreCurso(), m.getFecha(), m.getEstado());
    }
}
