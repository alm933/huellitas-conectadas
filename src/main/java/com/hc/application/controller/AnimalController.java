package com.hc.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hc.application.dto.animal.AnimalRequest;
import com.hc.application.dto.animal.AnimalResponse;
import com.hc.application.service.AnimalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/animales")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public ResponseEntity<List<AnimalResponse>> listar() {
        return ResponseEntity.ok(animalService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                animalService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<AnimalResponse> guardar(
            @Valid @RequestBody AnimalRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(animalService.guardar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AnimalRequest request) {

        return ResponseEntity.ok(
                animalService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        animalService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}