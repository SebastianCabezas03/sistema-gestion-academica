package com.reinodeloscielos.backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record MatriculaRequestDto(
        @NotNull @Positive Long alumnoId,
        @NotEmpty List<@NotNull @Positive Long> cursosIds,
        @NotNull @PastOrPresent LocalDate fecha) {}
