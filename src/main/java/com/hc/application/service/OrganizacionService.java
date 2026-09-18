package com.hc.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hc.application.dto.organizacion.OrganizacionRequest;
import com.hc.application.dto.organizacion.OrganizacionResponse;
import com.hc.application.entity.OrganizacionEntity;
import com.hc.application.exception.RecursoNoEncontradoException;
import com.hc.application.repository.OrganizacionRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizacionService {

    private final OrganizacionRepository organizacionRepository;

    public OrganizacionService(OrganizacionRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizacionResponse> listar() {
        return organizacionRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizacionResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerEntidad(id));
    }

    @Transactional
    public OrganizacionResponse guardar(OrganizacionRequest request) {

        OrganizacionEntity organizacion = new OrganizacionEntity();

        organizacion.setNombre(request.getNombre());
        organizacion.setTipo(request.getTipo());
        organizacion.setCorreo(request.getCorreo());
        organizacion.setTelefono(request.getTelefono());
        organizacion.setDireccion(request.getDireccion());
        organizacion.setDistrito(request.getDistrito());
        organizacion.setDescripcion(request.getDescripcion());
        organizacion.setFotoUrl(request.getFotoUrl());

        return convertirAResponse(
                organizacionRepository.save(organizacion)
        );
    }

    @Transactional
    public OrganizacionResponse actualizar(
            Long id,
            OrganizacionRequest request) {

        OrganizacionEntity organizacion = obtenerEntidad(id);

        organizacion.setNombre(request.getNombre());
        organizacion.setTipo(request.getTipo());
        organizacion.setCorreo(request.getCorreo());
        organizacion.setTelefono(request.getTelefono());
        organizacion.setDireccion(request.getDireccion());
        organizacion.setDistrito(request.getDistrito());
        organizacion.setDescripcion(request.getDescripcion());
        organizacion.setFotoUrl(request.getFotoUrl());

        return convertirAResponse(
                organizacionRepository.save(organizacion)
        );
    }

    @Transactional
    public void eliminar(Long id) {
        OrganizacionEntity organizacion = obtenerEntidad(id);
        organizacionRepository.delete(organizacion);
    }

    private OrganizacionEntity obtenerEntidad(Long id) {
        return organizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la organización con id: " + id));
    }

    private OrganizacionResponse convertirAResponse(
            OrganizacionEntity organizacion) {

        return new OrganizacionResponse(
                organizacion.getId(),
                organizacion.getNombre(),
                organizacion.getTipo(),
                organizacion.getCorreo(),
                organizacion.getTelefono(),
                organizacion.getDireccion(),
                organizacion.getDistrito(),
                organizacion.getDescripcion(),
                organizacion.getFotoUrl(),
                organizacion.getActivo(),
                organizacion.getFechaRegistro()
        );
    }
}