
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

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class CuentaService {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]{4,100}$");

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

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos de la cuenta son obligatorios"
            );
        }

        // Validar y normalizar nombre de usuario.
        if (request.username() == null || request.username().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de usuario es obligatorio"
            );
        }

        String username = request.username()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario debe tener entre 4 y 100 caracteres y solo puede contener letras, números, puntos, guiones y guiones bajos"
            );
        }

        if (usuarioRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre de usuario ya está registrado"
            );
        }

        // Validar contraseña.
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña es obligatoria"
            );
        }

        String password = request.password();

        int passwordBytes = password.getBytes(
                StandardCharsets.UTF_8
        ).length;

        if (password.length() < 8 || passwordBytes > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres y no superar los 72 bytes"
            );
        }

        // Validar rol.
        if (request.rol() == null || request.rol().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rol es obligatorio"
            );
        }

        String rol = request.rol()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!rol.equals("ALUMNO") && !rol.equals("DOCENTE")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden crear cuentas de alumnos o docentes"
            );
        }

        boolean esAlumno = rol.equals("ALUMNO");

        // Validar asociación según el rol.
        if (esAlumno &&
                (request.alumnoId() == null ||
                 request.alumnoId() <= 0 ||
                 request.docenteId() != null)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes seleccionar un alumno válido para esta cuenta"
            );
        }

        if (!esAlumno &&
                (request.docenteId() == null ||
                 request.docenteId() <= 0 ||
                 request.alumnoId() != null)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes seleccionar un docente válido para esta cuenta"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setUsername(username);
        usuario.setRol(rol);

        // Asociar la cuenta con su alumno o docente.
        if (esAlumno) {

            Alumno alumno = alumnoRepository
                    .findById(request.alumnoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Alumno no encontrado"
                    ));

            if (usuarioRepository.existsByAlumnoId(alumno.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Este alumno ya tiene una cuenta"
                );
            }

            usuario.setAlumno(alumno);

        } else {

            Docente docente = docenteRepository
                    .findById(request.docenteId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Docente no encontrado"
                    ));

            if (usuarioRepository.existsByDocenteId(docente.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Este docente ya tiene una cuenta"
                );
            }

            usuario.setDocente(docente);
        }

        // Cifrar contraseña antes de guardar.
        usuario.setPassword(passwordEncoder.encode(password));

        return usuarioRepository.save(usuario);
    }
}
