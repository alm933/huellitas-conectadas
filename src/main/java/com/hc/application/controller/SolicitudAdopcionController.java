package com.hc.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hc.application.dto.solicitud.SolicitudAdopcionRequest;
import com.hc.application.dto.solicitud.SolicitudAdopcionResponse;
import com.hc.application.dto.solicitud.SolicitudEstadoRequest;
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.service.SolicitudAdopcionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/solicitudes-adopcion")
public class SolicitudAdopcionController {

    private final SolicitudAdopcionService service;

    public SolicitudAdopcionController(SolicitudAdopcionService service) {
        this.service = service;
    }

    @PostMapping("/animales/{animalId}")
    public ResponseEntity<SolicitudAdopcionResponse> crear(
            @PathVariable Long animalId,
            @Valid @RequestBody(required = false) SolicitudAdopcionRequest request,
            @AuthenticationPrincipal UsuarioEntity adoptante) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(animalId, request, adoptante));
    }

    @GetMapping("/organizacion")
    public ResponseEntity<List<SolicitudAdopcionResponse>> listarParaOrganizacion(
            @AuthenticationPrincipal UsuarioEntity usuario) {
        return ResponseEntity.ok(service.listarParaOrganizacion(usuario));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SolicitudAdopcionResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudEstadoRequest request,
            @AuthenticationPrincipal UsuarioEntity usuario) {
        return ResponseEntity.ok(service.cambiarEstado(id, request, usuario));
    }
}
