package com.hc.application.dto.animal;

import com.hc.application.enums.EstadoAnimal;
import com.hc.application.enums.SexoAnimal;
import com.hc.application.enums.TamanoAnimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class AnimalRequest {

    @NotBlank (message = "El nombre es obligatorio")
    @Size(max = 80)
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    private String especie;

    @Size(max = 80)
    private String raza;

    @NotNull (message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    private Integer edadMeses;

    @NotNull(message = "El sexo es obligatorio")
    private SexoAnimal sexo;

    @NotNull(message = "El tamaño es obligatorio")
    private TamanoAnimal tamanio;

    @NotBlank(message = "La historia es obligatoria")
    private String descripcion;

    private String fotoUrl;

    private EstadoAnimal estado;

    @NotNull(message = "La organización es obligatoria")
    private Long organizacionId;

}
