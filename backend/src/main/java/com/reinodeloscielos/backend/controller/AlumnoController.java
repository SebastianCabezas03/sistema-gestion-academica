
package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.entity.Alumno;
import com.reinodeloscielos.backend.service.AlumnoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
@CrossOrigin(origins = "http://localhost:4200")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    // Listar todos los alumnos
    @GetMapping
    public List<Alumno> listar() {
        return alumnoService.listar();
    }

    // Buscar alumnos por nombres o apellidos
    @GetMapping("/buscar")
    public List<Alumno> buscar(@RequestParam String texto) {
        return alumnoService.buscar(texto);
    }

    // Buscar alumno por ID
    @GetMapping("/{id}")
    public Alumno buscarPorId(@PathVariable Long id) {
        return alumnoService.buscarPorId(id);
    }

    // Registrar alumno
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Alumno crear(@Valid @RequestBody Alumno alumno) {
        return alumnoService.crear(alumno);
    }

    // Actualizar alumno
    @PutMapping("/{id}")
    public Alumno actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Alumno alumno) {

        return alumnoService.actualizar(id, alumno);
    }

    // Eliminar alumno
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        alumnoService.eliminar(id);
    }
}
