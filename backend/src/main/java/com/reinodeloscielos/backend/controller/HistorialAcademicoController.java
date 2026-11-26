
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.HistorialAcademicoResponse;
import com.reinodeloscielos.backend.service.HistorialAcademicoService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alumnos")
@CrossOrigin(origins = "http://localhost:4200")
public class HistorialAcademicoController {

    private final HistorialAcademicoService historialAcademicoService;

    public HistorialAcademicoController(
            HistorialAcademicoService historialAcademicoService) {

        this.historialAcademicoService = historialAcademicoService;
    }

    // Consultar el historial académico del alumno autenticado
    @GetMapping("/mi-historial")
    public HistorialAcademicoResponse obtenerMiHistorial(
            Authentication authentication) {

        return historialAcademicoService.obtenerMiHistorial(authentication);
    }
}
