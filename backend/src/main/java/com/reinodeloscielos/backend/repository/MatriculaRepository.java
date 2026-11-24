package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Matricula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    @Override
    @Query(value = "select * from matriculas where id = ?1 for update", nativeQuery = true)
    Optional<Matricula> findById(Long id);

    boolean existsByAlumnoIdAndCursoIdAndEstado(Long alumnoId, Long cursoId, Matricula.Estado estado);
    boolean existsByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);
    boolean existsByAlumnoIdAndCursoIdAndIdNot(Long alumnoId, Long cursoId, Long id);
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

}
