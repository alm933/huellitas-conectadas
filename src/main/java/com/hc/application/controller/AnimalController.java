package com.hc.application.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hc.application.dto.animal.AnimalPublicacionResponse;
import com.hc.application.dto.animal.AnimalFotoResponse;
import com.hc.application.dto.animal.AnimalRequest;
import com.hc.application.dto.animal.AnimalResponse;
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.service.AnimalPublicacionService;
import com.hc.application.service.AnimalService;

import jakarta.validation.Valid;

@Tag(name = "Mascotas")
@RestController
@RequestMapping("/api/v1/animales")
public class AnimalController {

    private final AnimalPublicacionService animalPublicacionService;
    private final AnimalService animalService;

    public AnimalController(
            AnimalService animalService,
            AnimalPublicacionService animalPublicacionService) {
        this.animalService = animalService;
        this.animalPublicacionService = animalPublicacionService;
    }

    @GetMapping
    public ResponseEntity<List<AnimalResponse>> listar() {
        return ResponseEntity.ok(animalService.listar());
    }

    public record CatalogoPage(List<AnimalResponse> content, int number, int size,
            long totalElements, int totalPages) {}

    @Operation(summary = "Consultar el catálogo paginado de mascotas disponibles")
    @GetMapping("/catalogo")
    public CatalogoPage listarCatalogo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "TODAS") String especie) {
        if (page < 0 || size < 1 || size > 50 || (long) page * size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica una página válida y un tamaño entre 1 y 50.");
        }
        if (!List.of("TODAS", "PERRO", "GATO", "OTRO").contains(especie)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro de especie no válido.");
        }

        Page<AnimalResponse> result = animalService.listarCatalogo(page, size, especie);
        return new CatalogoPage(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/mios")
    public ResponseEntity<List<AnimalResponse>> listarPropios(
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity.ok(animalService.listarPropios(usuarioActual));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.buscarPorId(id));
    }

    @Operation(summary = "Listar fotos de una mascota")
    @GetMapping("/{id}/fotos")
    public ResponseEntity<List<AnimalFotoResponse>> listarFotos(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.listarFotos(id));
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

    @Operation(
            summary = "Publicar mascota con fotos",
            description = "Requiere rol ORGANIZACION. Envía animal como JSON y entre 2 y 8 fotos. "
                    + "La parte animal debe tener Content-Type application/json.")
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
