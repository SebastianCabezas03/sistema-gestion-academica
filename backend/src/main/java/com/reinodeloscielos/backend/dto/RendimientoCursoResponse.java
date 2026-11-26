
package com.reinodeloscielos.backend.dto;

import java.math.BigDecimal;

public record RendimientoCursoResponse(

        Long cursoId,

        String nombreCurso,

        Long totalCalificaciones,

        BigDecimal promedioGeneral,

        Long totalAsistencias,

        Long totalFaltas,

        Long totalTardanzas,

        Long totalRegistrosAsistencia,

        BigDecimal porcentajeAsistencia

) {}
