
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Matricula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    boolean existsByAlumnoIdAndCursoIdAndIdNot(
            Long alumnoId,
            Long cursoId,
            Long id
    );

    boolean existsByAlumnoIdAndCursoIdAndEstado(
            Long alumnoId,
            Long cursoId,
            Matricula.Estado estado
    );

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"curso", "curso.docente"})
    List<Matricula> findByAlumnoIdAndEstadoOrderByIdDesc(
            Long alumnoId,
            Matricula.Estado estado
    );

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findByCursoIdAndEstadoOrderByAlumnoApellidosAsc(
            Long cursoId,
            Matricula.Estado estado
    );

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findByCursoIdOrderByIdDesc(Long cursoId);
}
