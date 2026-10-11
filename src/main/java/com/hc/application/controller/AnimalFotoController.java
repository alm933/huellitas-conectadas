package com.hc.application.controller;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hc.application.entity.AnimalFotoEntity;
import com.hc.application.repository.AnimalFotoRepository;
import com.hc.application.service.GarageStorageService;

@RestController
public class AnimalFotoController {

    private final AnimalFotoRepository fotoRepository;
    private final GarageStorageService garageStorageService;

    public AnimalFotoController(
            AnimalFotoRepository fotoRepository,
            GarageStorageService garageStorageService) {
        this.fotoRepository = fotoRepository;
        this.garageStorageService = garageStorageService;
    }

    @GetMapping("/api/v1/fotos/{id}")
    public ResponseEntity<byte[]> obtener(@PathVariable Long id) {
        AnimalFotoEntity foto = fotoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró la foto."));

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(foto.getContentType()))
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                    .body(garageStorageService.descargar(foto.getGarageKey()));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo recuperar la foto desde Garage.", e);
        }
    }
}