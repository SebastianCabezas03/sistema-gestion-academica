
package com.reinodeloscielos.backend.service;

import com.reinodeloscielos.backend.entity.Docente;
import com.reinodeloscielos.backend.repository.DocenteRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;

    public DocenteService(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    // Listar todos los docentes
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    // Buscar docente por ID
    public Optional<Docente> buscarPorId(Long id) {
        return docenteRepository.findById(id);
    }

    // Registrar docente
    @Transactional
    public Docente guardar(Docente docente) {

        docente.setId(null);

        normalizarDatos(docente);
        validarDatosObligatorios(docente);
        validarCorreo(docente.getCorreo(), null);

        return docenteRepository.save(docente);
    }

    // Actualizar docente
    @Transactional
    public Docente actualizar(Long id, Docente datosDocente) {

        Docente existente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Docente no encontrado"
                ));

        normalizarDatos(datosDocente);
        validarDatosObligatorios(datosDocente);
        validarCorreo(datosDocente.getCorreo(), id);

        existente.setNombres(datosDocente.getNombres());
        existente.setApellidos(datosDocente.getApellidos());
        existente.setCorreo(datosDocente.getCorreo());
        existente.setTelefono(datosDocente.getTelefono());
        existente.setEspecialidad(datosDocente.getEspecialidad());

        return docenteRepository.save(existente);
    }

    // Eliminar docente
    @Transactional
    public void eliminar(Long id) {

        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Docente no encontrado"
                ));

        try {

            docenteRepository.delete(docente);
            docenteRepository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el docente porque tiene cursos o cuentas de usuario relacionadas"
            );
        }
    }

    // Normalizar los datos antes de guardarlos
    private void normalizarDatos(Docente docente) {

        if (docente.getNombres() != null) {
            docente.setNombres(docente.getNombres().trim());
        }

        if (docente.getApellidos() != null) {
            docente.setApellidos(docente.getApellidos().trim());
        }

        if (docente.getCorreo() != null) {

            String correo = docente.getCorreo().trim().toLowerCase();

            docente.setCorreo(
                    correo.isEmpty() ? null : correo
            );
        }

        if (docente.getTelefono() != null) {

            String telefono = docente.getTelefono().trim();

            docente.setTelefono(
                    telefono.isEmpty() ? null : telefono
            );
        }

        if (docente.getEspecialidad() != null) {
            docente.setEspecialidad(
                    docente.getEspecialidad().trim()
            );
        }
    }

    // Validar campos obligatorios
    private void validarDatosObligatorios(Docente docente) {

        if (docente.getNombres() == null ||
                docente.getNombres().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los nombres son obligatorios"
            );
        }

        if (docente.getApellidos() == null ||
                docente.getApellidos().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los apellidos son obligatorios"
            );
        }

        if (docente.getCorreo() == null ||
                docente.getCorreo().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico es obligatorio"
            );
        }

        if (docente.getEspecialidad() == null ||
                docente.getEspecialidad().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La especialidad es obligatoria"
            );
        }
    }

    // Validar que el correo no esté registrado en otro docente
    private void validarCorreo(String correo, Long idActual) {

        boolean duplicado = idActual == null
                ? docenteRepository.existsByCorreoIgnoreCase(correo)
                : docenteRepository.existsByCorreoIgnoreCaseAndIdNot(
                        correo,
                        idActual
                );

        if (duplicado) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un docente registrado con ese correo"
            );
        }
    }
}
