
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.ReporteMatriculaResponse;
import com.reinodeloscielos.backend.service.ReporteMatriculaService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/matriculas")
public class ReporteMatriculaController {

    private final ReporteMatriculaService reporteMatriculaService;

    public ReporteMatriculaController(
            ReporteMatriculaService reporteMatriculaService) {

        this.reporteMatriculaService = reporteMatriculaService;
    }

    // Obtener reporte de matrículas por curso
    @GetMapping("/curso/{cursoId}")
    public ReporteMatriculaResponse obtenerReporte(
            @PathVariable Long cursoId,
            Authentication authentication) {

        return reporteMatriculaService.obtenerReporte(
                cursoId,
                authentication
        );
    }
}
