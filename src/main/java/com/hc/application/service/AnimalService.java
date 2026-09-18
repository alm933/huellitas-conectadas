package com.hc.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hc.application.dto.animal.AnimalRequest;
import com.hc.application.dto.animal.AnimalResponse;
import com.hc.application.entity.AnimalEntity;
import com.hc.application.entity.OrganizacionEntity;
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
        AnimalEntity animal = obtenerEntidad(id);
        return convertirAResponse(animal);
    }

    @Transactional
    public AnimalResponse guardar(AnimalRequest request) {

        OrganizacionEntity organizacion = organizacionRepository
                .findById(request.getOrganizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la organización con id: "
                                + request.getOrganizacionId()));

        AnimalEntity animal = new AnimalEntity();

        animal.setNombre(request.getNombre());
        animal.setEspecie(request.getEspecie());
        animal.setRaza(request.getRaza());
        animal.setEdadMeses(request.getEdadMeses());
        animal.setSexo(request.getSexo());
        animal.setTamano(request.getTamanio());
        animal.setDescripcion(request.getDescripcion());
        animal.setFotoUrl(request.getFotoUrl());
        animal.setEstado(request.getEstado());
        animal.setOrganizacion(organizacion);

        AnimalEntity animalGuardado = animalRepository.save(animal);

        return convertirAResponse(animalGuardado);
    }

    @Transactional
    public AnimalResponse actualizar(Long id, AnimalRequest request) {

        AnimalEntity animal = obtenerEntidad(id);

        OrganizacionEntity organizacion = organizacionRepository
                .findById(request.getOrganizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la organización con id: "
                                + request.getOrganizacionId()));

        animal.setNombre(request.getNombre());
        animal.setEspecie(request.getEspecie());
        animal.setRaza(request.getRaza());
        animal.setEdadMeses(request.getEdadMeses());
        animal.setSexo(request.getSexo());
        animal.setTamano(request.getTamanio());
        animal.setDescripcion(request.getDescripcion());
        animal.setFotoUrl(request.getFotoUrl());
        animal.setOrganizacion(organizacion);

        if (request.getEstado() != null) {
            animal.setEstado(request.getEstado());
        }

        return convertirAResponse(animalRepository.save(animal));
    }

    @Transactional
    public void eliminar(Long id) {
        AnimalEntity animal = obtenerEntidad(id);
        animalRepository.delete(animal);
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
                animal.getOrganizacion().getNombre()
        );
    }
}