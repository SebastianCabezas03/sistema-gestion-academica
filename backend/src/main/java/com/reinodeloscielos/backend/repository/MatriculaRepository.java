
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Matricula;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    long countByEstado(Matricula.Estado estado);

    boolean existsByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    boolean existsByAlumnoIdAndCursoIdAndIdNot(
            Long alumnoId, Long cursoId, Long id);

    boolean existsByAlumnoIdAndCursoIdAndEstado(
            Long alumnoId, Long cursoId, Matricula.Estado estado);

    // Buscar una matrícula independientemente de su estado.
    Optional<Matricula> findByAlumnoIdAndCursoId(
            Long alumnoId, Long cursoId);

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"curso", "curso.docente"})
    List<Matricula> findByAlumnoIdAndEstadoOrderByIdDesc(
            Long alumnoId, Matricula.Estado estado);

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findByCursoIdAndEstadoOrderByAlumnoApellidosAsc(
            Long cursoId, Matricula.Estado estado);

    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findByCursoIdOrderByIdDesc(Long cursoId);

    // Historial completo de matrículas de un alumno.
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findByAlumnoIdOrderByIdDesc(Long alumnoId);
}
