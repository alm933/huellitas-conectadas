package com.hc.application.entity;

import java.time.LocalDateTime;

import com.hc.application.enums.TipoOrganizacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name="organizaciones")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class OrganizacionEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "El nombre es obligatorio")
    @Size(max=120,message = "El nombre no puede superara los 120 caracteres") 
    @Column(nullable = false , length=120)
    private String nombre;

    @NotNull (message = "El tipo de organización es obligatorio")
    @Enumerated (EnumType.STRING)
    @Column (nullable = false , length = 20)
    private TipoOrganizacion tipo;

    @NotBlank (message =  "El correo es obligatorio")
    @Email (message = "El correo no tiene un formato válido")
    @Column (nullable = false , unique = true , length = 150 )
    private String correo ;

    @NotBlank (message = "El telefono es obligatorio")
    @Column (nullable = false , length = 200)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false, length = 200)
    private String direccion;

    @NotBlank (message = "El distrito es obligatorio")
    @Column(nullable = false , length = 80)
    private String distrito;

    @Size (max = 500 , message = "La descripción no puede superar los 500 caracteres")
    @Column (length = 500)
    private String descripcion;
    
    @Column (name = "foto_url" , length = 500)
    private String fotoUrl;

    @Column (nullable = false)
    private Boolean activo = true;

    @Column (name = "fecha_registro",nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist 
    protected void alCrear(){
        fechaRegistro = LocalDateTime.now();

        if(activo== null){
            activo = true;
        }
    }
    
}
