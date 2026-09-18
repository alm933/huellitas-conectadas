package com.hc.application.dto.auth;

import com.hc.application.enums.UsuarioRol;

public record UsuarioResponse(
        Long id,
        String name,
        String lastName,
        String email,
        UsuarioRol role
) {
}
