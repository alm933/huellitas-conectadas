package com.hc.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.service.AnimalPublicacionService;
import com.hc.application.service.AnimalService;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import com.hc.application.dto.animal.AnimalPublicacionResponse;



import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/animales")
public class AnimalController {

    private final AnimalPublicacionService animalPublicacionService;
    private final AnimalService animalService;
    
    public AnimalController(
        AnimalService animalService,
        AnimalPublicacionService animalPublicacionService) 
    {
        this.animalService = animalService;
        this.animalPublicacionService = animalPublicacionService;
    }



    @GetMapping
    public ResponseEntity<List<AnimalResponse>> listar() {
        return ResponseEntity.ok(animalService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(animalService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AnimalResponse> guardar(
            @Valid @RequestBody AnimalRequest request,
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(animalService.guardar(request, usuarioActual));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AnimalRequest request,
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity.ok(
                animalService.actualizar(id, request, usuarioActual));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        animalService.eliminar(id, usuarioActual);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
        path = "/publicar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalPublicacionResponse> publicar(
        @Valid @RequestPart("animal") AnimalRequest request,
        @RequestPart("fotos") List<MultipartFile> fotos,
        @AuthenticationPrincipal UsuarioEntity usuarioActual) {

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(animalPublicacionService.publicar(
                    request, fotos, usuarioActual));
    }

}