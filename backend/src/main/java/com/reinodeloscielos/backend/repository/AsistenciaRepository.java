
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Asistencia;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository
        extends JpaRepository<Asistencia, Long> {

    // Contar asistencias según su estado.
    long countByEstado(Asistencia.Estado estado);

    // Consultar todas las asistencias de un alumno.
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByAlumnoIdOrderByFechaDesc(Long alumnoId);

    // Consultar asistencias de un curso en una fecha específica.
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByCursoIdAndFechaOrderByAlumnoApellidosAsc(
            Long cursoId,
            LocalDate fecha
    );

    // Verificar si ya existe una asistencia para alumno, curso y fecha.
    boolean existsByAlumnoIdAndCursoIdAndFecha(
            Long alumnoId,
            Long cursoId,
            LocalDate fecha
    );

    // Consultar asistencias de un alumno en un curso específico.
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByAlumnoIdAndCursoIdOrderByFechaDesc(
            Long alumnoId,
            Long cursoId
    );

    // Consultar todas las asistencias ordenadas por fecha.
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findAllByOrderByFechaDesc();

    // Consultar asistencias de un curso dentro de un rango de fechas.
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByCursoIdAndFechaBetweenOrderByFechaDesc(
            Long cursoId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    // Consultar todas las asistencias de un curso.
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByCursoIdOrderByFechaDesc(Long cursoId);
}
