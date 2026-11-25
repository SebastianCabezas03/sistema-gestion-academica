
package com.reinodeloscielos.backend.dto;

public record ResumenAsistenciaResponse(
        Long alumnoId,
        String nombreAlumno,
        Long cursoId,
        String nombreCurso,
        long totalRegistros,
        long asistencias,
        long faltas,
        long tardanzas,
        double porcentajeAsistencia
) {}
