
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.EvaluacionRequest;
import com.reinodeloscielos.backend.dto.EvaluacionResponse;
import com.reinodeloscielos.backend.service.EvaluacionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    // Registrar una evaluación
    @PostMapping
    public ResponseEntity<EvaluacionResponse> crear(
            @Valid @RequestBody EvaluacionRequest request,
            Authentication authentication) {

        EvaluacionResponse response =
                evaluacionService.crear(request, authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Listar evaluaciones de un curso
    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<EvaluacionResponse>> listarPorCurso(
            @PathVariable Long cursoId,
            Authentication authentication) {

        return ResponseEntity.ok(
                evaluacionService.listarPorCurso(cursoId, authentication)
        );
    }

    // Actualizar una evaluación
    @PutMapping("/{evaluacionId}")
    public ResponseEntity<EvaluacionResponse> actualizar(
            @PathVariable Long evaluacionId,
            @Valid @RequestBody EvaluacionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                evaluacionService.actualizar(
                        evaluacionId,
                        request,
                        authentication
                )
        );
    }

    // Eliminar una evaluación
    @DeleteMapping("/{evaluacionId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long evaluacionId,
            Authentication authentication) {

        evaluacionService.eliminar(evaluacionId, authentication);

        return ResponseEntity.noContent().build();
    }
}
