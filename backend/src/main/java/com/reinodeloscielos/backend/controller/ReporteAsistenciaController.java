
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.ReporteCursoAsistenciaResponse;
import com.reinodeloscielos.backend.dto.ReporteCursoPeriodoResponse;
import com.reinodeloscielos.backend.dto.ResumenAsistenciaResponse;
import com.reinodeloscielos.backend.service.ReporteAsistenciaService;
import com.reinodeloscielos.backend.service.ReporteCursoAsistenciaService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes/asistencias")
public class ReporteAsistenciaController {

    private final ReporteAsistenciaService reporteAsistenciaService;
    private final ReporteCursoAsistenciaService reporteCursoAsistenciaService;

    public ReporteAsistenciaController(
            ReporteAsistenciaService reporteAsistenciaService,
            ReporteCursoAsistenciaService reporteCursoAsistenciaService) {

        this.reporteAsistenciaService = reporteAsistenciaService;
        this.reporteCursoAsistenciaService = reporteCursoAsistenciaService;
    }

    @GetMapping("/alumno/{alumnoId}/curso/{cursoId}")
    public ResumenAsistenciaResponse obtenerResumenAlumno(
            @PathVariable Long alumnoId,
            @PathVariable Long cursoId,
            Authentication authentication) {

        return reporteAsistenciaService.obtenerResumen(
                alumnoId,
                cursoId,
                authentication
        );
    }

    @GetMapping("/curso/{cursoId}")
    public ReporteCursoAsistenciaResponse obtenerReporteCurso(
            @PathVariable Long cursoId,
            Authentication authentication) {

        return reporteCursoAsistenciaService.obtenerReporte(
                cursoId,
                authentication
        );
    }

    @GetMapping("/curso/{cursoId}/periodo")
    public ReporteCursoPeriodoResponse obtenerReportePorPeriodo(
            @PathVariable Long cursoId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,
            Authentication authentication) {

        return reporteCursoAsistenciaService.obtenerReportePorPeriodo(
                cursoId,
                fechaInicio,
                fechaFin,
                authentication
        );
    }
}
