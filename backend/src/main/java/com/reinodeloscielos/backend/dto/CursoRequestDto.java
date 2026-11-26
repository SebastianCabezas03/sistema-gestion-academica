
package com.reinodeloscielos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CursoRequestDto {

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Size(
            max = 100,
            message = "El nombre del curso no puede superar los 100 caracteres"
    )
    private String nombre;

    @NotNull(message = "Debe seleccionar un docente para el curso")
    private Long idDocente;

    public CursoRequestDto() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Long idDocente) {
        this.idDocente = idDocente;
    }
}
