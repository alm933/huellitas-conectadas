package com.hc.application.dto.auth;

import com.hc.application.enums.TipoOrganizacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroOrganizacionRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String pass;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotNull
    private TipoOrganizacion tipo;

    @NotBlank
    @Email
    @Size(max = 150)
    private String correo;

    @NotBlank
    @Size(max = 200)
    private String telefono;

    @NotBlank
    @Size(max = 200)
    private String direccion;

    @NotBlank
    @Size(max = 80)
    private String distrito;

    @Size(max = 500)
    private String descripcion;
}