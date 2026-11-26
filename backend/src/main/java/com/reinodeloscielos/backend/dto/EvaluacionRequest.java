
package com.reinodeloscielos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EvaluacionRequest(

        @NotBlank(message = "El nombre de la evaluación es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "La fecha de la evaluación es obligatoria")
        LocalDate fecha,

        @NotNull(message = "El curso es obligatorio")
        Long cursoId

) {
}
