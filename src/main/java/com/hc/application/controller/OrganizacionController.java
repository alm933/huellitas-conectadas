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

import com.hc.application.dto.organizacion.OrganizacionRequest;
import com.hc.application.dto.organizacion.OrganizacionResponse;
import com.hc.application.service.OrganizacionService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/v1/organizaciones")
public class OrganizacionController {

    private final OrganizacionService organizacionService;

    public OrganizacionController(OrganizacionService organizacionService) {
        this.organizacionService = organizacionService;
    }

    @GetMapping 
    public ResponseEntity<List<OrganizacionResponse>> listar() {
        return ResponseEntity.ok(organizacionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizacionResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                organizacionService.buscarPorId(id)
        );
    }

    @PostMapping 
    public ResponseEntity<OrganizacionResponse> guardar(
            @Valid @RequestBody OrganizacionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(organizacionService.guardar(request));
    }

    @PutMapping ("/{id}")
    public ResponseEntity<OrganizacionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrganizacionRequest request) {

        return ResponseEntity.ok(
                organizacionService.actualizar(id, request)
        );
    }

    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        organizacionService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}