package com.hc.application.dto.organizacion;

import java.time.LocalDateTime;

import com.hc.application.enums.TipoOrganizacion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter 
@AllArgsConstructor 
public class OrganizacionResponse {
    private Long id;
    private String nombre;
    private TipoOrganizacion tipo;
    private String correo;
    private String telefono;
    private String direccion;
    private String distrito;
    private String descripcion;
    private String fotoUrl;
    private Boolean activo;
    private LocalDateTime fechaRegistro;

}
