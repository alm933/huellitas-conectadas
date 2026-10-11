package com.hc.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hc.application.entity.UsuarioEntity;
import com.hc.application.repository.UsuarioRepository;

import com.hc.application.dto.auth.RegistroRequest;
import com.hc.application.dto.auth.UsuarioResponse;
//import com.hc.application.entity.UsuarioEntity;
import com.hc.application.enums.UsuarioRol;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hc.application.dto.auth.RegistroOrganizacionRequest;
import com.hc.application.entity.OrganizacionEntity;
import com.hc.application.repository.OrganizacionRepository;



@Service
public class UsuarioService {

    private final OrganizacionRepository organizacionRepository;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
        UsuarioRepository usuarioRepository,
        OrganizacionRepository organizacionRepository,
        PasswordEncoder passwordEncoder) {

    this.usuarioRepository = usuarioRepository;
    this.organizacionRepository = organizacionRepository;
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

@Transactional
public UsuarioResponse registrarOrganizacion(
        RegistroOrganizacionRequest request) {

    if (usuarioRepository.existsByEmail(request.getEmail())) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT, "El correo de acceso ya está registrado");
    }

    if (organizacionRepository.existsByCorreo(request.getCorreo())) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT, "El correo de contacto ya está registrado");
    }

    UsuarioEntity usuario = UsuarioEntity.builder()
            .name(request.getName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .pass(passwordEncoder.encode(request.getPass()))
            .role(UsuarioRol.ORGANIZACION)
            .activo(true)
            .build();

    usuario = usuarioRepository.save(usuario);

    OrganizacionEntity organizacion = new OrganizacionEntity();
    organizacion.setNombre(request.getNombre());
    organizacion.setTipo(request.getTipo());
    organizacion.setCorreo(request.getCorreo());
    organizacion.setTelefono(request.getTelefono());
    organizacion.setDireccion(request.getDireccion());
    organizacion.setDistrito(request.getDistrito());
    organizacion.setDescripcion(request.getDescripcion());
    organizacion.setActivo(false);
    organizacion.setUsuario(usuario);

    organizacionRepository.save(organizacion);

    return new UsuarioResponse(
            usuario.getId(),
            usuario.getName(),
            usuario.getLastName(),
            usuario.getEmail(),
            usuario.getRole()
    );
}



}