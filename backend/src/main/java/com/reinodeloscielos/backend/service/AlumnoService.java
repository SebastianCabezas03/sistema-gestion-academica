
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.repository.AlumnoRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;

    public AlumnoService(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    // Listar todos los alumnos
    public List<Alumno> listar() {
        return alumnoRepository.findAll();
    }

    // Buscar alumno por identificador
    public Alumno buscarPorId(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno no encontrado"
                ));
    }

    // Buscar alumnos por nombres o apellidos
    public List<Alumno> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return listar();
        }

        String busqueda = texto.trim();

        return alumnoRepository
                .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                        busqueda,
                        busqueda
                );
    }

    // Registrar alumno
    @Transactional
    public Alumno crear(Alumno alumno) {

        alumno.setId(null);

        normalizarDatos(alumno);
        validarDatosObligatorios(alumno);

        validarCorreoDuplicado(alumno.getCorreo(), null);
        validarTelefonoDuplicado(alumno.getTelefono(), null);

        return alumnoRepository.save(alumno);
    }

    // Actualizar alumno
    @Transactional
    public Alumno actualizar(Long id, Alumno datos) {

        Alumno alumno = buscarPorId(id);

        normalizarDatos(datos);
        validarDatosObligatorios(datos);

        validarCorreoDuplicado(datos.getCorreo(), id);
        validarTelefonoDuplicado(datos.getTelefono(), id);

        alumno.setNombres(datos.getNombres());
        alumno.setApellidos(datos.getApellidos());
        alumno.setCorreo(datos.getCorreo());
        alumno.setTelefono(datos.getTelefono());

        return alumnoRepository.save(alumno);
    }

    // Eliminar alumno
    @Transactional
    public void eliminar(Long id) {

        Alumno alumno = buscarPorId(id);

        try {

            alumnoRepository.delete(alumno);
            alumnoRepository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el alumno porque tiene matrículas, asistencias o calificaciones relacionadas"
            );
        }
    }

    // Normalizar información antes de guardarla
    private void normalizarDatos(Alumno alumno) {

        if (alumno.getNombres() != null) {
            alumno.setNombres(alumno.getNombres().trim());
        }

        if (alumno.getApellidos() != null) {
            alumno.setApellidos(alumno.getApellidos().trim());
        }

        if (alumno.getCorreo() != null) {

            String correo = alumno.getCorreo().trim().toLowerCase();

            alumno.setCorreo(correo.isEmpty() ? null : correo);
        }

        if (alumno.getTelefono() != null) {

            String telefono = alumno.getTelefono().trim();

            alumno.setTelefono(
                    telefono.isEmpty() ? null : telefono
            );
        }
    }

    // Validar información obligatoria
    private void validarDatosObligatorios(Alumno alumno) {

        if (alumno.getNombres() == null ||
                alumno.getNombres().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los nombres son obligatorios"
            );
        }

        if (alumno.getApellidos() == null ||
                alumno.getApellidos().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los apellidos son obligatorios"
            );
        }

        if (alumno.getCorreo() == null ||
                alumno.getCorreo().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico es obligatorio"
            );
        }
    }

    // Validar correo duplicado
    private void validarCorreoDuplicado(String correo, Long idActual) {

        boolean duplicado = idActual == null
                ? alumnoRepository.existsByCorreoIgnoreCase(correo)
                : alumnoRepository.existsByCorreoIgnoreCaseAndIdNot(
                        correo,
                        idActual
                );

        if (duplicado) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un alumno con ese correo electrónico"
            );
        }
    }

    // Validar teléfono duplicado
    private void validarTelefonoDuplicado(
            String telefono,
            Long idActual) {

        if (telefono == null || telefono.isBlank()) {
            return;
        }

        boolean duplicado = idActual == null
                ? alumnoRepository.existsByTelefono(telefono)
                : alumnoRepository.existsByTelefonoAndIdNot(
                        telefono,
                        idActual
                );

        if (duplicado) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un alumno con ese teléfono"
            );
        }
    }
}
