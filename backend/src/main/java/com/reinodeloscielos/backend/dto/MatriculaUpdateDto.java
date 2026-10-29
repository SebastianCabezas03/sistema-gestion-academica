package com.reinodeloscielos.backend.dto;

import com.reinodeloscielos.backend.entity.Matricula.Estado;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record MatriculaUpdateDto(
        @NotNull @Positive Long alumnoId,
        @NotNull @Positive Long cursoId,
        @NotNull @PastOrPresent LocalDate fecha,
        @NotNull Estado estado) {}
