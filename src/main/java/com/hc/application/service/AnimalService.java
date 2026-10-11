package com.hc.application.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hc.application.dto.animal.AnimalRequest;
import com.hc.application.dto.animal.AnimalResponse;
import com.hc.application.entity.AnimalEntity;
import com.hc.application.entity.OrganizacionEntity;
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.exception.RecursoNoEncontradoException;
import com.hc.application.repository.AnimalRepository;
import com.hc.application.repository.OrganizacionRepository;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final OrganizacionRepository organizacionRepository;

    public AnimalService(
            AnimalRepository animalRepository,
            OrganizacionRepository organizacionRepository) {
        this.animalRepository = animalRepository;
        this.organizacionRepository = organizacionRepository;
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> listar() {
        return animalRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerEntidad(id));
    }

    @Transactional
    public AnimalResponse guardar(AnimalRequest request, UsuarioEntity usuarioActual) {
        OrganizacionEntity organizacion = obtenerOrganizacionActiva(usuarioActual);

        AnimalEntity animal = new AnimalEntity();
        aplicarDatos(animal, request);
        animal.setOrganizacion(organizacion);

        return convertirAResponse(animalRepository.save(animal));
    }

    @Transactional
    public AnimalResponse actualizar(
            Long id,
            AnimalRequest request,
            UsuarioEntity usuarioActual) {
        OrganizacionEntity organizacionActual =
                obtenerOrganizacionActiva(usuarioActual);
        AnimalEntity animal = obtenerEntidad(id);
        validarPropiedad(animal, organizacionActual);

        aplicarDatos(animal, request);
        if (request.getEstado() != null) {
            animal.setEstado(request.getEstado());
        }

        return convertirAResponse(animalRepository.save(animal));
    }

    @Transactional
    public void eliminar(Long id, UsuarioEntity usuarioActual) {
        OrganizacionEntity organizacionActual =
                obtenerOrganizacionActiva(usuarioActual);
        AnimalEntity animal = obtenerEntidad(id);
        validarPropiedad(animal, organizacionActual);
        animalRepository.delete(animal);
    }

    private void aplicarDatos(AnimalEntity animal, AnimalRequest request) {
        animal.setNombre(request.getNombre());
        animal.setEspecie(request.getEspecie());
        animal.setRaza(request.getRaza());
        animal.setEdadMeses(request.getEdadMeses());
        animal.setSexo(request.getSexo());
        animal.setTamano(request.getTamanio());
        animal.setDescripcion(request.getDescripcion());
    }

    private OrganizacionEntity obtenerOrganizacionActiva(
            UsuarioEntity usuarioActual) {
        if (usuarioActual == null || usuarioActual.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }

        OrganizacionEntity organizacion = organizacionRepository
                .findByUsuario_Id(usuarioActual.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "La cuenta no está vinculada a una organización."));

        if (!Boolean.TRUE.equals(organizacion.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La organización debe estar activa para gestionar mascotas.");
        }

        return organizacion;
    }

    private void validarPropiedad(
            AnimalEntity animal,
            OrganizacionEntity organizacionActual) {
        if (animal.getOrganizacion() == null
                || !animal.getOrganizacion().getId()
                        .equals(organizacionActual.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes modificar mascotas de otra organización.");
        }
    }

    private AnimalEntity obtenerEntidad(Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el animal con id: " + id));
    }

    private AnimalResponse convertirAResponse(AnimalEntity animal) {
        return new AnimalResponse(
                animal.getId(),
                animal.getNombre(),
                animal.getEspecie(),
                animal.getRaza(),
                animal.getEdadMeses(),
                animal.getSexo(),
                animal.getTamano(),
                animal.getDescripcion(),
                animal.getFotoUrl(),
                animal.getEstado(),
                animal.getFechaPublicacion(),
                animal.getOrganizacion().getId(),
                animal.getOrganizacion().getNombre());
    }
}