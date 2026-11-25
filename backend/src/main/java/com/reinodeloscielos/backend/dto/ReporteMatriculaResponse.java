
package com.reinodeloscielos.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteMatriculaResponse(
        Long cursoId,
        String nombreCurso,
        int totalMatriculas,
        int matriculasActivas,
        int matriculasCanceladas,
        List<DetalleMatricula> matriculas
) {

    public record DetalleMatricula(
            Long matriculaId,
            Long alumnoId,
            String nombreAlumno,
            LocalDate fecha,
            String estado
    ) {}
}
