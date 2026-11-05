package com.hc.application.dto.solicitud;

import jakarta.validation.constraints.Size;

public record SolicitudAdopcionRequest(
        @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres.")
        String mensaje,
        @jakarta.validation.constraints.NotNull(message = "Indica si tienes otras mascotas.")
        Boolean tieneOtrasMascotas,
        @jakarta.validation.constraints.NotBlank(message = "Indica tu tipo de vivienda.")
        @Size(max = 30, message = "El tipo de vivienda no puede superar los 30 caracteres.")
        String tipoVivienda) {
}
