package com.reinodeloscielos.backend.dto;

public record PerfilResponse(
        String username,
        String rol,
        Long personaId,
        String nombres,
        String apellidos,
        String correo,
        String telefono,
        String especialidad
) {
}
