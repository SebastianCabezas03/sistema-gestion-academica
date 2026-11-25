
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.AsistenciaResponse;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.service.GestionAsistenciaService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asistencias")
public class GestionAsistenciaController {

    private final GestionAsistenciaService gestionAsistenciaService;

    public GestionAsistenciaController(
            GestionAsistenciaService gestionAsistenciaService) {

        this.gestionAsistenciaService = gestionAsistenciaService;
    }

    @PutMapping("/{asistenciaId}")
    public AsistenciaResponse actualizar(
            @PathVariable Long asistenciaId,
            @RequestBody ActualizarAsistenciaRequest request,
            Authentication authentication) {

        Asistencia asistencia = gestionAsistenciaService.actualizar(
                asistenciaId,
                request.estado(),
                authentication
        );

        return convertir(asistencia);
    }

    @DeleteMapping("/{asistenciaId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long asistenciaId,
            Authentication authentication) {

        gestionAsistenciaService.eliminar(
                asistenciaId,
                authentication
        );

        return ResponseEntity.noContent().build();
    }

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

    public record ActualizarAsistenciaRequest(
            Asistencia.Estado estado
    ) {}
}
