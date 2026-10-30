package com.hc.application.dto.solicitud;

import com.hc.application.enums.EstadoSolicitudAdopcion;
import jakarta.validation.constraints.NotNull;

public record SolicitudEstadoRequest(
        @NotNull(message = "Selecciona el estado de la solicitud.")
        EstadoSolicitudAdopcion estado) {
}
