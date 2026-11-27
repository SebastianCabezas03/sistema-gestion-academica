
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.MatriculaRequestDto;
import com.reinodeloscielos.backend.dto.MatriculaResponseDto;
import com.reinodeloscielos.backend.dto.MatriculaUpdateDto;

import com.reinodeloscielos.backend.service.MatriculaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matriculas")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class MatriculaController {

    private final MatriculaService service;

    public MatriculaController(MatriculaService service) {
        this.service = service;
    }

    // Listar todas las matrículas.
    @GetMapping
    public List<MatriculaResponseDto> listar() {
        return service.listar();
    }

    // Registrar matrículas.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatriculaResponseDto> registrar(
            @Valid @RequestBody MatriculaRequestDto dto) {

        return service.registrar(dto);
    }

    // Actualizar una matrícula.
    @PutMapping("/{id}")
    public MatriculaResponseDto actualizar(
            @PathVariable @Positive(message = "El ID debe ser positivo") Long id,
            @Valid @RequestBody MatriculaUpdateDto dto) {

        return service.actualizar(id, dto);
    }

    // Cancelar una matrícula.
    @PatchMapping("/{id}/cancelar")
    public MatriculaResponseDto cancelar(
            @PathVariable @Positive(message = "El ID debe ser positivo") Long id) {

        return service.cancelar(id);
    }
}
