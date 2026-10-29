package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.*;
import com.reinodeloscielos.backend.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/matriculas")
@CrossOrigin(origins = "http://localhost:4200")
public class MatriculaController {
    private final MatriculaService service;
    public MatriculaController(MatriculaService service) { this.service = service; }

    @GetMapping
    public List<MatriculaResponseDto> listar() { return service.listar(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public List<MatriculaResponseDto> registrar(@Valid @RequestBody MatriculaRequestDto dto) {
        return service.registrar(dto);
    }
    @PutMapping("/{id}")
    public MatriculaResponseDto actualizar(@PathVariable Long id, @Valid @RequestBody MatriculaUpdateDto dto) {
        return service.actualizar(id, dto);
    }
    @PatchMapping("/{id}/cancelar")
    public MatriculaResponseDto cancelar(@PathVariable Long id) { return service.cancelar(id); }
}
