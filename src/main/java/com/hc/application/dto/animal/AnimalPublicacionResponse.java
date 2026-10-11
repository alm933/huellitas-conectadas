package com.hc.application.dto.animal;

import java.util.List;

public record AnimalPublicacionResponse(
        AnimalResponse animal,
        List<AnimalFotoResponse> fotos) {
}