
package com.reinodeloscielos.backend.dto;

import java.math.BigDecimal;

public record RendimientoAcademicoResponse(

        Long totalCalificaciones,

        BigDecimal promedioGeneral,

        Long totalAsistencias,

        Long totalFaltas,

        Long totalTardanzas,

        Long totalRegistrosAsistencia,

        BigDecimal porcentajeAsistencia

) {}
