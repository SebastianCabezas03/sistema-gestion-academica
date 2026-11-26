
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.RendimientoAcademicoResponse;
import com.reinodeloscielos.backend.service.RendimientoAcademicoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/rendimiento")
public class RendimientoAcademicoController {

    private final RendimientoAcademicoService rendimientoAcademicoService;

    public RendimientoAcademicoController(
            RendimientoAcademicoService rendimientoAcademicoService) {

        this.rendimientoAcademicoService = rendimientoAcademicoService;
    }

    @GetMapping
    public ResponseEntity<RendimientoAcademicoResponse> obtenerRendimiento(
            Authentication authentication) {

        return ResponseEntity.ok(
                rendimientoAcademicoService.obtenerRendimiento(authentication)
        );
    }
}
