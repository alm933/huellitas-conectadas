package com.hc.application.dto.solicitud;

import jakarta.validation.constraints.Size;

public record SolicitudAdopcionRequest(
        @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres.")
        String mensaje) {
}
