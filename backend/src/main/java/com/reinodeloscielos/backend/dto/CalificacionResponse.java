
package com.reinodeloscielos.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CalificacionResponse(
        Long id,
        Long alumnoId,
        String nombreAlumno,
        Long evaluacionId,
        String nombreEvaluacion,
        String nombreCurso,
        LocalDate fechaEvaluacion,
        BigDecimal nota
) {
}
