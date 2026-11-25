
package com.reinodeloscielos.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteCursoPeriodoResponse(
        Long cursoId,
        String nombreCurso,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int totalAlumnos,
        List<ResumenAsistenciaResponse> alumnos
) {}
