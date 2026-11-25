
package com.reinodeloscielos.backend.dto;

import java.util.List;

public record ReporteCursoAsistenciaResponse(
        Long cursoId,
        String nombreCurso,
        int totalAlumnos,
        List<ResumenAsistenciaResponse> alumnos
) {}
