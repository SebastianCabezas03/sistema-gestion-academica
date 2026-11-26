
package com.reinodeloscielos.backend.dto;

public record DashboardResponse(

        Long totalAlumnos,

        Long totalDocentes,

        Long totalCursos,

        Long totalMatriculas,

        Long matriculasActivas,

        Long matriculasCanceladas,

        Long totalAsistencias,

        Long totalEvaluaciones,

        Long totalCalificaciones

) {}
