
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.DashboardResponse;
import com.reinodeloscielos.backend.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Consultar estadísticas generales
    @GetMapping("/estadisticas")
    public ResponseEntity<DashboardResponse> obtenerEstadisticas(
            Authentication authentication) {

        return ResponseEntity.ok(
                dashboardService.obtenerEstadisticas(authentication)
        );
    }
}
