
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.CalificacionNotaRequest;
import com.reinodeloscielos.backend.dto.CalificacionRequest;
import com.reinodeloscielos.backend.dto.CalificacionResponse;
import com.reinodeloscielos.backend.service.CalificacionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calificaciones")
@Validated
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(
            CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    // Registrar una calificación.
    @PostMapping
    public ResponseEntity<CalificacionResponse> registrar(
            @Valid @RequestBody CalificacionRequest request,
            Authentication authentication) {

        CalificacionResponse response =
                calificacionService.registrar(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Listar calificaciones de una evaluación.
    @GetMapping("/evaluacion/{evaluacionId}")
    public ResponseEntity<List<CalificacionResponse>> listarPorEvaluacion(
            @PathVariable @Positive Long evaluacionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                calificacionService.listarPorEvaluacion(
                        evaluacionId,
                        authentication
                )
        );
    }

    // Actualizar una calificación.
    @PutMapping("/{calificacionId}")
    public ResponseEntity<CalificacionResponse> actualizar(
            @PathVariable @Positive Long calificacionId,
            @Valid @RequestBody CalificacionNotaRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                calificacionService.actualizar(
                        calificacionId,
                        request,
                        authentication
                )
        );
    }

    // Eliminar una calificación.
    @DeleteMapping("/{calificacionId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive Long calificacionId,
            Authentication authentication) {

        calificacionService.eliminar(
                calificacionId,
                authentication
        );

        return ResponseEntity.noContent().build();
    }

    // Consultar calificaciones del alumno autenticado.
    @GetMapping("/mis-calificaciones")
    public ResponseEntity<List<CalificacionResponse>> listarMisCalificaciones(
            Authentication authentication) {

        return ResponseEntity.ok(
                calificacionService.listarMisCalificaciones(authentication)
        );
    }
}
