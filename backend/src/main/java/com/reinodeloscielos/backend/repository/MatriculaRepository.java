package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Matricula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    boolean existsByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);
    boolean existsByAlumnoIdAndCursoIdAndIdNot(Long alumnoId, Long cursoId, Long id);
    @EntityGraph(attributePaths = {"alumno", "curso"})
    List<Matricula> findAllByOrderByIdDesc();
}
