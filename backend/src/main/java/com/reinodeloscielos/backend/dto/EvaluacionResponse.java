
package com.reinodeloscielos.backend.dto;

import java.time.LocalDate;

public record EvaluacionResponse(
        Long id,
        String nombre,
        String descripcion,
        LocalDate fecha,
        Long cursoId,
        String nombreCurso
) {
}
