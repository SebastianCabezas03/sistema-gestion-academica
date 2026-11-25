
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.CursoRequestDto;
import com.reinodeloscielos.backend.dto.CursoResponseDto;
import com.reinodeloscielos.backend.service.CursoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "http://localhost:4200")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public ResponseEntity<List<CursoResponseDto>> listarCursos() {
        return ResponseEntity.ok(cursoService.listarCursos());
    }

    @PostMapping
    public ResponseEntity<CursoResponseDto> guardarCurso(
            @RequestBody CursoRequestDto cursoDto) {

        CursoResponseDto curso = cursoService.guardarCurso(cursoDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(curso);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDto> actualizarCurso(
            @PathVariable Long id,
            @RequestBody CursoRequestDto cursoDto) {

        CursoResponseDto curso = cursoService.actualizarCurso(id, cursoDto);

        return ResponseEntity.ok(curso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCurso(
            @PathVariable Long id) {

        cursoService.eliminarCurso(id);

        return ResponseEntity.noContent().build();
    }
}
