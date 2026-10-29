package com.reinodeloscielos.backend.dto;

import com.reinodeloscielos.backend.entity.Matricula.Estado;
import java.time.LocalDate;

public record MatriculaResponseDto(Long id, Long alumnoId, String nombreAlumno,
        Long cursoId, String nombreCurso, LocalDate fecha, Estado estado) {}
