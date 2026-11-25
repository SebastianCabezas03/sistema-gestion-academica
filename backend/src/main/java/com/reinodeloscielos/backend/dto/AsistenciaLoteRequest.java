
package com.reinodeloscielos.backend.dto;

import com.reinodeloscielos.backend.entity.Asistencia;

import java.time.LocalDate;
import java.util.List;

public record AsistenciaLoteRequest(
        Long cursoId,
        LocalDate fecha,
        List<Detalle> asistencias
) {

    public record Detalle(
            Long alumnoId,
            Asistencia.Estado estado
    ) {}
}
