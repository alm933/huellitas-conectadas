package com.hc.application.dto.solicitud;

import java.time.LocalDateTime;

import com.hc.application.enums.EstadoSolicitudAdopcion;

public record SolicitudAdopcionResponse(
        Long id,
        Long animalId,
        String animalNombre,
        String adoptanteNombre,
        String adoptanteCorreo,
        String mensaje,
        EstadoSolicitudAdopcion estado,
        LocalDateTime fechaSolicitud) {
}
