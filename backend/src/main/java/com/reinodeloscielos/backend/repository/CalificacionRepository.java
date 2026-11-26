
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Calificacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    @EntityGraph(attributePaths = {"alumno", "evaluacion", "evaluacion.curso"})
    List<Calificacion> findByEvaluacionIdOrderByAlumnoApellidosAsc(Long evaluacionId);

    @EntityGraph(attributePaths = {"alumno", "evaluacion", "evaluacion.curso"})
    List<Calificacion> findByAlumnoIdOrderByEvaluacionFechaDesc(Long alumnoId);

    @EntityGraph(attributePaths = {"alumno", "evaluacion", "evaluacion.curso"})
    Optional<Calificacion> findByIdAndEvaluacionCursoId(Long id, Long cursoId);

    boolean existsByAlumnoIdAndEvaluacionId(Long alumnoId, Long evaluacionId);

    @EntityGraph(attributePaths = {"alumno", "evaluacion", "evaluacion.curso"})
    List<Calificacion> findByAlumnoIdAndEvaluacionCursoIdOrderByEvaluacionFechaDesc(
            Long alumnoId, Long cursoId
    );
}
