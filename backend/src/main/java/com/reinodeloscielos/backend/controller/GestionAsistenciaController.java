
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.AsistenciaResponse;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.service.GestionAsistenciaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/asistencias")
public class GestionAsistenciaController {

    private final GestionAsistenciaService gestionAsistenciaService;

    public GestionAsistenciaController(
            GestionAsistenciaService gestionAsistenciaService) {

        this.gestionAsistenciaService = gestionAsistenciaService;
    }

    // Actualizar el estado de una asistencia.
    @PutMapping("/{asistenciaId}")
    public AsistenciaResponse actualizar(

            @PathVariable
            @Positive(message = "El ID de asistencia debe ser positivo")
            Long asistenciaId,

            @Valid @RequestBody ActualizarAsistenciaRequest request,

            Authentication authentication) {

        Asistencia asistencia = gestionAsistenciaService.actualizar(
                asistenciaId,
                request.estado(),
                authentication
        );

        return convertir(asistencia);
    }

    // Eliminar un registro de asistencia.
    @DeleteMapping("/{asistenciaId}")
    public ResponseEntity<Void> eliminar(

            @PathVariable
            @Positive(message = "El ID de asistencia debe ser positivo")
            Long asistenciaId,

            Authentication authentication) {

        gestionAsistenciaService.eliminar(
                asistenciaId,
                authentication
        );

        return ResponseEntity.noContent().build();
    }

    // Convertir la entidad en un DTO de respuesta.
    private AsistenciaResponse convertir(Asistencia asistencia) {

        return new AsistenciaResponse(
                asistencia.getId(),
                asistencia.getAlumno().getId(),
                asistencia.getAlumno().getNombres() + " "
                        + asistencia.getAlumno().getApellidos(),
                asistencia.getCurso().getId(),
                asistencia.getCurso().getNombreCurso(),
                asistencia.getFecha(),
                asistencia.getEstado().name()
        );
    }

    // DTO para recibir la actualización del estado.
    public record ActualizarAsistenciaRequest(

            @NotNull(message = "Debes indicar el nuevo estado de asistencia")
            Asistencia.Estado estado

    ) {}
}
