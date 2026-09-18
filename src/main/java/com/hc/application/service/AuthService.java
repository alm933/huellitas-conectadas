package com.hc.application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.hc.application.dto.auth.AuthResponse;
import com.hc.application.dto.auth.LoginRequest;
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {

        var autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPass()
                )
        );

        UsuarioEntity usuario =
                (UsuarioEntity) autenticacion.getPrincipal();

        String token = jwtService.generarToken(usuario);

        return new AuthResponse(
                token,
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }
}