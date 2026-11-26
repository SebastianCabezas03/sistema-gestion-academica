

package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.repository.AlumnoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;

    public AlumnoService(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    public List<Alumno> listar() {
        return alumnoRepository.findAll();
    }

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

    public Alumno crear(Alumno alumno) {
        normalizarCorreo(alumno);
        validarCorreoDuplicado(alumno.getCorreo(), null);
        validarTelefonoDuplicado(alumno.getTelefono(), null);
        alumno.setId(null);
        return alumnoRepository.save(alumno);
    }

    public Alumno actualizar(Long id, Alumno datos) {
        Alumno alumno = buscarPorId(id);

        normalizarCorreo(datos);
        validarCorreoDuplicado(datos.getCorreo(), id);
        validarTelefonoDuplicado(datos.getTelefono(), id);

        alumno.setNombres(datos.getNombres());
        alumno.setApellidos(datos.getApellidos());
        alumno.setCorreo(datos.getCorreo());
        alumno.setTelefono(datos.getTelefono());

        return alumnoRepository.save(alumno);
    }

    public void eliminar(Long id) {
        Alumno alumno = buscarPorId(id);
        alumnoRepository.delete(alumno);
    }

    private void normalizarCorreo(Alumno alumno) {
        if (alumno.getCorreo() != null) {
            String correo = alumno.getCorreo().trim();
            alumno.setCorreo(correo.isEmpty() ? null : correo);
        }
    }

    private void validarCorreoDuplicado(String correo, Long idActual) {
        if (correo == null || correo.isBlank()) {
            return;
        }

        boolean duplicado = idActual == null
                ? alumnoRepository.existsByCorreoIgnoreCase(correo)
                : alumnoRepository.existsByCorreoIgnoreCaseAndIdNot(
                        correo, idActual);

        if (duplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un alumno con ese correo electrónico"
            );
        }
    }

    private void validarTelefonoDuplicado(String telefono, Long idActual) {
        if (telefono == null || telefono.isBlank()) {
            return;
        }

        String telefonoNormalizado = telefono.trim();

        boolean duplicado = idActual == null
                ? alumnoRepository.existsByTelefono(telefonoNormalizado)
                : alumnoRepository.existsByTelefonoAndIdNot(
                        telefonoNormalizado, idActual);

        if (duplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un alumno con ese teléfono"
            );
        }
    }
}
