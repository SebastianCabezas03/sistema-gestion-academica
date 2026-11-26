
package com.reinodeloscielos.backend.service.impl;

import com.reinodeloscielos.backend.dto.CursoRequestDto;
import com.reinodeloscielos.backend.dto.CursoResponseDto;
import com.reinodeloscielos.backend.entity.Curso;
import com.reinodeloscielos.backend.entity.Docente;
import com.reinodeloscielos.backend.repository.CursoRepository;
import com.reinodeloscielos.backend.repository.DocenteRepository;
import com.reinodeloscielos.backend.service.CursoService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;

    public CursoServiceImpl(
            CursoRepository cursoRepository,
            DocenteRepository docenteRepository) {

        this.cursoRepository = cursoRepository;
        this.docenteRepository = docenteRepository;
    }

    // Registrar un curso
    @Override
    @Transactional
    public CursoResponseDto guardarCurso(CursoRequestDto curso) {

        validarNombre(curso.getNombre());

        Docente docente = buscarDocente(curso.getIdDocente());

        Curso nuevoCurso = new Curso();

        nuevoCurso.setNombreCurso(curso.getNombre().trim());
        nuevoCurso.setDocente(docente);

        Curso cursoGuardado = cursoRepository.save(nuevoCurso);

        return toDto(cursoGuardado);
    }

    // Listar todos los cursos
    @Override
    @Transactional(readOnly = true)
    public List<CursoResponseDto> listarCursos() {

        return cursoRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Eliminar un curso
    @Override
    @Transactional
    public void eliminarCurso(Long id) {

        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        try {

            cursoRepository.delete(curso);
            cursoRepository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el curso porque tiene matrículas, evaluaciones o asistencias relacionadas"
            );
        }
    }

    // Actualizar un curso
    @Override
    @Transactional
    public CursoResponseDto actualizarCurso(
            Long id,
            CursoRequestDto curso) {

        Curso cursoActualizado = cursoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Curso no encontrado"
                ));

        validarNombre(curso.getNombre());

        Docente docente = buscarDocente(curso.getIdDocente());

        cursoActualizado.setNombreCurso(curso.getNombre().trim());
        cursoActualizado.setDocente(docente);

        Curso actualizado = cursoRepository.save(cursoActualizado);

        return toDto(actualizado);
    }

    // Buscar docente y comprobar que exista
    private Docente buscarDocente(Long idDocente) {

        if (idDocente == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe asignarse un docente al curso"
            );
        }

        return docenteRepository.findById(idDocente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Docente no encontrado"
                ));
    }

    // Validar nombre del curso
    private void validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del curso es obligatorio"
            );
        }

        if (nombre.trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del curso no puede superar los 100 caracteres"
            );
        }
    }

    // Convertir entidad a DTO
    private CursoResponseDto toDto(Curso curso) {

        CursoResponseDto dto = new CursoResponseDto();

        dto.setId(curso.getId());
        dto.setNombre(curso.getNombreCurso());

        if (curso.getDocente() != null) {
            dto.setIdDocente(curso.getDocente().getId());
        } else {
            dto.setIdDocente(null);
        }

        return dto;
    }
}
