package com.hc.application.dto.organizacion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarEstadoOrganizacionRequest {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
