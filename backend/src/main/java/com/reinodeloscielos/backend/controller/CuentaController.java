package com.reinodeloscielos.backend.controller;

import com.reinodeloscielos.backend.dto.CrearCuentaRequest;
import com.reinodeloscielos.backend.entity.Usuario;
import com.reinodeloscielos.backend.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "http://localhost:4200")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crear(@Valid @RequestBody CrearCuentaRequest request) {
        Usuario usuario = cuentaService.crear(request);

        return Map.of(
                "mensaje", "Cuenta creada correctamente",
                "username", usuario.getUsername(),
                "rol", usuario.getRol()
        );
    }
}

