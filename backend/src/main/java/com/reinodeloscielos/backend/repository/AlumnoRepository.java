
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    // Validaciones de correo y teléfono
    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    boolean existsByTelefono(String telefono);

    boolean existsByTelefonoAndIdNot(String telefono, Long id);

    // Búsquedas de alumnos
    List<Alumno> findByNombresContainingIgnoreCase(String nombres);

    List<Alumno> findByApellidosContainingIgnoreCase(String apellidos);

    List<Alumno> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String nombres,
            String apellidos
    );
}
