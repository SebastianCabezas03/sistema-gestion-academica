
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.AsistenciaLoteRequest;
import com.reinodeloscielos.backend.dto.AsistenciaResponse;
import com.reinodeloscielos.backend.entity.Asistencia;
import com.reinodeloscielos.backend.service.AsistenciaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    // Consultar las asistencias del alumno autenticado.
    @GetMapping("/mi-asistencia")
    public List<AsistenciaResponse> miAsistencia(
            Authentication authentication) {

        return asistenciaService.miAsistencia(authentication);
    }

    // Consultar las asistencias de un alumno.
    @GetMapping("/alumno/{alumnoId}")
    public List<AsistenciaResponse> asistenciaAlumno(

            @PathVariable
            @Positive(message = "El ID del alumno debe ser positivo")
            Long alumnoId,

            Authentication authentication) {

        return asistenciaService.asistenciaAlumno(
                alumnoId,
                authentication
        );
    }

    // Consultar todas las asistencias (administrador).
    @GetMapping("/admin/todas")
    public List<AsistenciaResponse> todasLasAsistencias(
            Authentication authentication) {

        return asistenciaService.todasLasAsistencias(authentication);
    }

    // Consultar las asistencias de un curso en una fecha.
    @GetMapping("/curso/{cursoId}")
    public List<AsistenciaResponse> asistenciaCurso(

            @PathVariable
            @Positive(message = "El ID del curso debe ser positivo")
            Long cursoId,

            @RequestParam
            @NotNull(message = "Debes indicar la fecha")
            @PastOrPresent(message = "La fecha no puede ser futura")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            Authentication authentication) {

        return asistenciaService.asistenciaCurso(
                cursoId,
                fecha,
                authentication
        );
    }

    // Registrar la asistencia de un alumno.
    @PostMapping("/registrar")
    public AsistenciaResponse registrar(
            @Valid @RequestBody RegistrarAsistenciaRequest request,
            Authentication authentication) {

        return asistenciaService.registrar(
                request.alumnoId(),
                request.cursoId(),
                request.fecha(),
                request.estado(),
                authentication
        );
    }

    // Registrar las asistencias de varios alumnos.
    @PostMapping("/registrar/lote")
    public List<AsistenciaResponse> registrarLote(
            @Valid @RequestBody AsistenciaLoteRequest request,
            Authentication authentication) {

        return asistenciaService.registrarLote(
                request,
                authentication
        );
    }

    // DTO para recibir el registro individual de asistencia.
    public record RegistrarAsistenciaRequest(

            @NotNull(message = "Debes indicar el alumno")
            @Positive(message = "El ID del alumno debe ser positivo")
            Long alumnoId,

            @NotNull(message = "Debes indicar el curso")
            @Positive(message = "El ID del curso debe ser positivo")
            Long cursoId,

            @NotNull(message = "Debes indicar la fecha de asistencia")
            @PastOrPresent(message = "La fecha no puede ser futura")
            LocalDate fecha,

            @NotNull(message = "Debes indicar el estado de asistencia")
            Asistencia.Estado estado

    ) {}
}
