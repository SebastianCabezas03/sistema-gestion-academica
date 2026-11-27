
package com.reinodeloscielos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearCuentaRequest(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(
                min = 4,
                max = 100,
                message = "El nombre de usuario debe tener entre 4 y 100 caracteres"
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos"
        )
        String username,

        @NotBlank(message = "La contraseña temporal es obligatoria")
        @Size(
                min = 8,
                max = 100,
                message = "La contraseña debe tener entre 8 y 100 caracteres"
        )
        String password,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(
                regexp = "(?i)DOCENTE|ALUMNO",
                message = "El rol debe ser DOCENTE o ALUMNO"
        )
        String rol,

        @Positive(message = "El ID del alumno debe ser positivo")
        Long alumnoId,

        @Positive(message = "El ID del docente debe ser positivo")
        Long docenteId

) {
}
