
package com.reinodeloscielos.backend.repository;

import com.reinodeloscielos.backend.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByAlumnoId(Long alumnoId);

    boolean existsByDocenteId(Long docenteId);
}
