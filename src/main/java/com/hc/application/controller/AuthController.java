package com.hc.application.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hc.application.dto.auth.AuthResponse;
import com.hc.application.dto.auth.LoginRequest;
import com.hc.application.dto.auth.RegistroRequest;
import com.hc.application.dto.auth.UsuarioResponse;
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.security.JwtAuthenticationFilter;
import com.hc.application.service.AuthService;
import com.hc.application.service.UsuarioService;

import com.hc.application.dto.auth.RegistroOrganizacionRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    @Value("${app.jwt.expiration}")
    private long expiration;

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

    public record BrowserSession(String email, String role) {}

    @PostMapping("/login/browser")
    public ResponseEntity<BrowserSession> loginBrowser(
            @RequestHeader(JwtAuthenticationFilter.BROWSER_HEADER) String browserRequest,
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, sessionCookie(auth.token(), Duration.ofMillis(expiration)))
                .body(new BrowserSession(auth.email(), auth.role()));
    }

    @GetMapping("/session")
    public ResponseEntity<BrowserSession> session(@AuthenticationPrincipal UsuarioEntity user) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new BrowserSession(user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(JwtAuthenticationFilter.BROWSER_HEADER) String browserRequest) {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO))
                .build();
    }

    private String sessionCookie(String token, Duration maxAge) {
        return ResponseCookie.from(JwtAuthenticationFilter.SESSION_COOKIE, token)
                .httpOnly(true).secure(true).sameSite("Lax").path("/").maxAge(maxAge)
                .build().toString();
    }

    @PostMapping("/register/organizacion")
public ResponseEntity<UsuarioResponse> registrarOrganizacion(
        @Valid @RequestBody RegistroOrganizacionRequest request) {

    return ResponseEntity
            .status(201)
            .body(usuarioService.registrarOrganizacion(request));
}

}