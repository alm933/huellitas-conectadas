package com.hc.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hc.application.entity.UsuarioEntity;
import com.hc.application.repository.UsuarioRepository;

import com.hc.application.dto.auth.RegistroRequest;
import com.hc.application.dto.auth.UsuarioResponse;
//import com.hc.application.entity.UsuarioEntity;
import com.hc.application.enums.UsuarioRol;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioEntity registrar(UsuarioEntity usuario)
    {

    if (usuarioRepository.existsByEmail(usuario.getEmail())) {
        throw new RuntimeException("El correo ya está registrado");
    }

    usuario.setPass(
            passwordEncoder.encode(usuario.getPass())
    );

    return usuarioRepository.save(usuario);
    
    }

public UsuarioResponse registrar(RegistroRequest request) {

    if (usuarioRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("El correo ya está registrado");
    }

    UsuarioEntity usuario = UsuarioEntity.builder()
            .name(request.getName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .pass(passwordEncoder.encode(request.getPass()))
            .role(UsuarioRol.ADOPTANTE)
            .activo(true)
            .build();

    UsuarioEntity guardado = usuarioRepository.save(usuario);

    return new UsuarioResponse(
            guardado.getId(),
            guardado.getName(),
            guardado.getLastName(),
            guardado.getEmail(),
            guardado.getRole()
    );
}


}