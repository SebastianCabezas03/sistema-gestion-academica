
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.RendimientoCursoResponse;
import com.reinodeloscielos.backend.service.RendimientoCursoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class RendimientoCursoController {

    private final RendimientoCursoService rendimientoCursoService;

    public RendimientoCursoController(
            RendimientoCursoService rendimientoCursoService) {

        this.rendimientoCursoService = rendimientoCursoService;
    }

    @GetMapping("/rendimiento/cursos")
    public ResponseEntity<List<RendimientoCursoResponse>>
            obtenerRendimientoPorCurso(
                    Authentication authentication) {

        return ResponseEntity.ok(
                rendimientoCursoService.obtenerRendimientoPorCurso(
                        authentication
                )
        );
    }
}
