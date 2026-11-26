
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Evaluacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    @EntityGraph(attributePaths = {"curso", "curso.docente"})
    List<Evaluacion> findByCursoIdOrderByFechaDesc(Long cursoId);

    @EntityGraph(attributePaths = {"curso", "curso.docente"})
    Optional<Evaluacion> findByIdAndCursoId(Long id, Long cursoId);

    boolean existsByCursoIdAndNombreIgnoreCase(Long cursoId, String nombre);
}
