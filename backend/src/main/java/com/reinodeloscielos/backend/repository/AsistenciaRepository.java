
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository
        extends JpaRepository<Asistencia, Long> {
long countByEstado(Asistencia.Estado estado);
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByAlumnoIdOrderByFechaDesc(Long alumnoId);

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByCursoIdAndFechaOrderByAlumnoApellidosAsc(
            Long cursoId,
            LocalDate fecha
    );

    boolean existsByAlumnoIdAndCursoIdAndFecha(
            Long alumnoId,
            Long cursoId,
            LocalDate fecha
    );

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByAlumnoIdAndCursoIdOrderByFechaDesc(
            Long alumnoId,
            Long cursoId
    );

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Asistencia> findByCursoIdAndFechaBetweenOrderByFechaDesc(
            Long cursoId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}
