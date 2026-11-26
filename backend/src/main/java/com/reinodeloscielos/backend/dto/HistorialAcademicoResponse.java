
package com.reinodeloscielos.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record HistorialAcademicoResponse(
        DatosAlumno alumno,
        ResumenAcademico resumen,
        List<DetalleMatricula> matriculas,
        List<DetalleAsistencia> asistencias
) {

    public record DatosAlumno(
            Long id,
            String nombres,
            String apellidos,
            String correo,
            String telefono
    ) {}

    public record ResumenAcademico(
            int totalMatriculas,
            int matriculasActivas,
            int matriculasCanceladas,
            int totalAsistencias,
            int asistencias,
            int faltas,
            int tardanzas,
            double porcentajeAsistencia
    ) {}

    public record DetalleMatricula(
            Long id,
            String nombreCurso,
            LocalDate fecha,
            String estado
    ) {}

    public record DetalleAsistencia(
            Long id,
            String nombreCurso,
            LocalDate fecha,
            String estado
    ) {}
}
