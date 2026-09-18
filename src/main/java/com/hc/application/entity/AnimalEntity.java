package com.hc.application.entity;

import java.time.LocalDateTime;

import com.hc.application.enums.EstadoAnimal;
import com.hc.application.enums.SexoAnimal;
import com.hc.application.enums.TamanoAnimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "animales")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class AnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80 , message = "El nombre no puede superar los 80 caracteres")
    @Column(nullable = false , length = 80)
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    @Column (nullable = false , length = 50)
    private String especie;

    @Size(max = 80 , message = "La raza no puede superar los 80 caracteres")
    @Column(length = 80)
    private String raza;

    @NotNull(message = "La edad es obligatoria")
    @Min (value = 0 , message = "La  edad no puede ser negativa")
    @Column(name = "edad_meses", nullable = false)
    private Integer edadMeses;

    @NotNull(message = "Ël sexo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 20)
    private SexoAnimal sexo;

    @NotNull(message = "El tamañio es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20) 
    private TamanoAnimal tamano;

    @NotBlank(message ="La historia del animal es obligatoria")
    @Column (columnDefinition = "TEXT",nullable = false)
    private String descripcion;
    
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @NotNull(message="El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 25)
    private EstadoAnimal estado;

    @Column(name = "fecha_publicacion",nullable = false)
    private LocalDateTime fechaPublicacion;



    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private OrganizacionEntity organizacion;

    @PrePersist 
    protected void alCrear(){
        fechaPublicacion = LocalDateTime.now();

        if(estado == null){
            estado = EstadoAnimal.DISPONIBLE;
        }
    }




}
