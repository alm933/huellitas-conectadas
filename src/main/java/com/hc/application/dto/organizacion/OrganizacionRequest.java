package com.hc.application.dto.organizacion;

import com.hc.application.enums.TipoOrganizacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class OrganizacionRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size (max=120)
    private String nombre;

    @NotNull (message = "El tipo de organización es obligatorio ")
    private TipoOrganizacion tipo;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    @Size(max = 500)
    private String descripcion;

    private String fotoUrl;

}
