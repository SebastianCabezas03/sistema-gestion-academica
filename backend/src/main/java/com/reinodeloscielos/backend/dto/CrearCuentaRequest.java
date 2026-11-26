
package com.reinodeloscielos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearCuentaRequest(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 100, message = "El nombre de usuario no puede superar los 100 caracteres")
        String username,

        @NotBlank(message = "La contraseña temporal es obligatoria")
        @Size(
                min = 8,
                max = 72,
                message = "La contraseña debe tener entre 8 y 72 caracteres"
        )
        String password,

        @NotBlank(message = "El rol es obligatorio")
        @Size(max = 20, message = "El rol no puede superar los 20 caracteres")
        String rol,

        Long alumnoId,

        Long docenteId

) {
}
