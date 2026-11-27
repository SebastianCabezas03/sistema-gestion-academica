

package com.reinodeloscielos.backend.controller;

import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = MatriculaController.class)
public class MatriculaExceptionHandler {

    // Errores de lógica de negocio.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> negocio(
            ResponseStatusException ex) {

        String mensaje = ex.getReason() == null
                ? "Solicitud inválida"
                : ex.getReason();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("message", mensaje));
    }

    // Errores de validación de los DTO.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(
            MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Los datos enviados no son válidos.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", mensaje));
    }

    // Errores de validación de parámetros de las rutas.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> validarParametros(
            ConstraintViolationException ex) {

        String mensaje = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(error -> error.getMessage())
                .orElse("Los parámetros enviados no son válidos.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", mensaje));
    }

    // Errores de integridad de la base de datos.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(
            DataIntegrityViolationException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "message",
                        "No se pudo guardar: la matrícula ya existe o el alumno/curso dejó de estar disponible. Actualiza la lista."
                ));
    }
}
