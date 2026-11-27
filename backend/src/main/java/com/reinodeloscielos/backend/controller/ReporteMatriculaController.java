
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.ReporteMatriculaResponse;
import com.reinodeloscielos.backend.service.ReporteMatriculaService;

import jakarta.validation.constraints.Positive;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/matriculas")
@Validated
public class ReporteMatriculaController {

    private final ReporteMatriculaService reporteMatriculaService;

    public ReporteMatriculaController(
            ReporteMatriculaService reporteMatriculaService) {

        this.reporteMatriculaService = reporteMatriculaService;
    }

    // Obtener reporte de matrículas por curso.
    @GetMapping("/curso/{cursoId}")
    public ReporteMatriculaResponse obtenerReporte(
            @PathVariable
            @Positive(message = "El ID del curso debe ser positivo")
            Long cursoId,
            Authentication authentication) {

        return reporteMatriculaService.obtenerReporte(
                cursoId,
                authentication
        );
    }
}
