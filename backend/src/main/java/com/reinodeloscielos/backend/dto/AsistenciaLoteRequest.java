
package com.reinodeloscielos.backend.dto;

import com.reinodeloscielos.backend.entity.Asistencia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record AsistenciaLoteRequest(

        @NotNull(message = "Debes seleccionar un curso")
        @Positive(message = "El ID del curso debe ser positivo")
        Long cursoId,

        @NotNull(message = "Debes indicar la fecha de asistencia")
        @PastOrPresent(message = "La fecha no puede ser futura")
        LocalDate fecha,

        @NotEmpty(message = "Debes registrar al menos una asistencia")
        List<@NotNull @Valid Detalle> asistencias

) {

    public record Detalle(

            @NotNull(message = "Debes indicar el alumno")
            @Positive(message = "El ID del alumno debe ser positivo")
            Long alumnoId,

            @NotNull(message = "Debes indicar el estado de asistencia")
            Asistencia.Estado estado

    ) {}
}
