package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.dto.CrearCuentaRequest;
import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.entity.Docente;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.repository.AlumnoRepository;
import com.reinodeloscielos.backend.repository.DocenteRepository;
import com.reinodeloscielos.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CuentaService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final DocenteRepository docenteRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaService(
            UsuarioRepository usuarioRepository,
            AlumnoRepository alumnoRepository,
            DocenteRepository docenteRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.docenteRepository = docenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario crear(CrearCuentaRequest request) {
        String username = request.username().trim();

        if (usuarioRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre de usuario ya está registrado");
        }

        String rol = request.rol().trim().toUpperCase();

        if (!rol.equals("ALUMNO") && !rol.equals("DOCENTE")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden crear cuentas de alumnos o docentes");
        }

        boolean esAlumno = rol.equals("ALUMNO");

        if (esAlumno && (request.alumnoId() == null || request.docenteId() != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes seleccionar un alumno para esta cuenta");
        }

        if (!esAlumno && (request.docenteId() == null || request.alumnoId() != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes seleccionar un docente para esta cuenta");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);

        if (esAlumno) {
            Alumno alumno = alumnoRepository.findById(request.alumnoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Alumno no encontrado"));

            boolean vinculado = usuarioRepository.findAll().stream()
                    .anyMatch(u -> u.getAlumno() != null
                            && u.getAlumno().getId().equals(alumno.getId()));

            if (vinculado) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Este alumno ya tiene una cuenta");
            }

            usuario.setAlumno(alumno);
        } else {
            Docente docente = docenteRepository.findById(request.docenteId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Docente no encontrado"));

            boolean vinculado = usuarioRepository.findAll().stream()
                    .anyMatch(u -> u.getDocente() != null
                            && u.getDocente().getId().equals(docente.getId()));

            if (vinculado) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Este docente ya tiene una cuenta");
            }

            usuario.setDocente(docente);
        }

        return usuarioRepository.save(usuario);
    }
}
