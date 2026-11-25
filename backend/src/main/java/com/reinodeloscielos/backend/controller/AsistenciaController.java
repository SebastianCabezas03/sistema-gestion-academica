
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.AsistenciaLoteRequest;
import com.reinodeloscielos.backend.dto.AsistenciaResponse;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.service.AsistenciaService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/mi-asistencia")
    public List<AsistenciaResponse> miAsistencia(
            Authentication authentication) {

        return asistenciaService.miAsistencia(authentication);
    }

    @GetMapping("/alumno/{alumnoId}")
    public List<AsistenciaResponse> asistenciaAlumno(
            @PathVariable Long alumnoId,
            Authentication authentication) {

        return asistenciaService.asistenciaAlumno(
                alumnoId,
                authentication
        );
    }

    @GetMapping("/admin/todas")
    public List<AsistenciaResponse> todasLasAsistencias(
            Authentication authentication) {

        return asistenciaService.todasLasAsistencias(authentication);
    }

    @GetMapping("/curso/{cursoId}")
    public List<AsistenciaResponse> asistenciaCurso(
            @PathVariable Long cursoId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            Authentication authentication) {

        return asistenciaService.asistenciaCurso(
                cursoId,
                fecha,
                authentication
        );
    }

    @PostMapping("/registrar")
    public AsistenciaResponse registrar(
            @RequestBody RegistrarAsistenciaRequest request,
            Authentication authentication) {

        return asistenciaService.registrar(
                request.alumnoId(),
                request.cursoId(),
                request.fecha(),
                request.estado(),
                authentication
        );
    }

    @PostMapping("/registrar/lote")
    public List<AsistenciaResponse> registrarLote(
            @RequestBody AsistenciaLoteRequest request,
            Authentication authentication) {

        return asistenciaService.registrarLote(
                request,
                authentication
        );
    }

    public record RegistrarAsistenciaRequest(
            Long alumnoId,
            Long cursoId,
            LocalDate fecha,
            Asistencia.Estado estado
    ) {}
}
