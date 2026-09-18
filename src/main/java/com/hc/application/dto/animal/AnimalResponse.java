package com.hc.application.dto.animal;

import java.time.LocalDateTime;

import com.hc.application.enums.EstadoAnimal;
import com.hc.application.enums.SexoAnimal;
import com.hc.application.enums.TamanoAnimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter 
@AllArgsConstructor 

public class AnimalResponse {

    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private Integer edadMeses;
    private SexoAnimal sexo;
    private TamanoAnimal tamanio;
    private String descripcion;
    private String fotoUrl;
    private EstadoAnimal estado;
    private LocalDateTime fechaPublicacion;
    private Long organizacionId;
    private String organizacionNombre;

}
