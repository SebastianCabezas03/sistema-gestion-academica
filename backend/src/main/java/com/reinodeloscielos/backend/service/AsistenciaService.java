package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.AsistenciaLoteRequest;
import com.reinodeloscielos.backend.dto.AsistenciaResponse;
import com.reinodeloscielos.backend.entity.Asistencia;

import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

public interface AsistenciaService {

    List<AsistenciaResponse> miAsistencia(Authentication authentication);

    List<AsistenciaResponse> asistenciaAlumno(
            Long alumnoId,
            Authentication authentication
    );

    List<AsistenciaResponse> todasLasAsistencias(
            Authentication authentication
    );

    List<AsistenciaResponse> asistenciaCurso(
            Long cursoId,
            LocalDate fecha,
            Authentication authentication
    );

    AsistenciaResponse registrar(
            Long alumnoId,
            Long cursoId,
            LocalDate fecha,
            Asistencia.Estado estado,
            Authentication authentication
    );

    List<AsistenciaResponse> registrarLote(
            AsistenciaLoteRequest request,
            Authentication authentication
    );
}

