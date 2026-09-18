package com.hc.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hc.application.dto.auth.AuthResponse;
import com.hc.application.dto.auth.LoginRequest;
import com.hc.application.dto.auth.RegistroRequest;
import com.hc.application.dto.auth.UsuarioResponse;
import com.hc.application.service.AuthService;
import com.hc.application.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {

        return ResponseEntity
                .status(201)
                .body(usuarioService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }
}