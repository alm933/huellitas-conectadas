package com.hc.application.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hc.application.dto.solicitud.SolicitudAdopcionRequest;
import com.hc.application.dto.solicitud.SolicitudAdopcionResponse;
import com.hc.application.dto.solicitud.SolicitudEstadoRequest;
import com.hc.application.entity.AnimalEntity;
import com.hc.application.entity.OrganizacionEntity;
import com.hc.application.entity.SolicitudAdopcionEntity;
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.enums.EstadoAnimal;
import com.hc.application.enums.EstadoSolicitudAdopcion;
import com.hc.application.repository.AnimalRepository;
import com.hc.application.repository.OrganizacionRepository;
import com.hc.application.repository.SolicitudAdopcionRepository;

@Service
public class SolicitudAdopcionService {
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private final AnimalRepository animalRepository;
    private final OrganizacionRepository organizacionRepository;
    private final SolicitudAdopcionRepository solicitudRepository;

    public SolicitudAdopcionService(
            AnimalRepository animalRepository,
            OrganizacionRepository organizacionRepository,
            SolicitudAdopcionRepository solicitudRepository) {
        this.animalRepository = animalRepository;
        this.organizacionRepository = organizacionRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Transactional
    public SolicitudAdopcionResponse crear(
            Long animalId, SolicitudAdopcionRequest request, UsuarioEntity adoptante) {
        validarUsuario(adoptante);
        AnimalEntity animal = animalRepository.findByIdForUpdate(animalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la mascota."));
        if (animal.getEstado() != EstadoAnimal.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta mascota ya no está disponible para solicitudes.");
        }
        if (!Boolean.TRUE.equals(animal.getOrganizacion().getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La organización de esta mascota no está activa.");
        }
        if (solicitudRepository.existsByAnimal_IdAndAdoptante_IdAndEstado(
                animalId, adoptante.getId(), EstadoSolicitudAdopcion.PENDIENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una solicitud pendiente para esta mascota.");
        }

        SolicitudAdopcionEntity solicitud = new SolicitudAdopcionEntity();
        solicitud.setAnimal(animal);
        solicitud.setAdoptante(adoptante);
        solicitud.setMensaje(request == null ? null : limpiarMensaje(request.mensaje()));
        solicitud.setEstado(EstadoSolicitudAdopcion.PENDIENTE);
        return convertir(solicitudRepository.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<SolicitudAdopcionResponse> listarPropias(UsuarioEntity usuario) {
        validarUsuario(usuario);
        return solicitudRepository.findByAdoptante_IdOrderByFechaSolicitudDesc(usuario.getId())
                .stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudAdopcionResponse> listarParaOrganizacion(UsuarioEntity usuario) {
        OrganizacionEntity organizacion = obtenerOrganizacion(usuario);
        return solicitudRepository.findByAnimal_Organizacion_IdOrderByFechaSolicitudDesc(organizacion.getId())
                .stream().map(this::convertir).toList();
    }

    @Transactional
    public SolicitudAdopcionResponse cambiarEstado(
            Long solicitudId, SolicitudEstadoRequest request, UsuarioEntity usuario) {
        OrganizacionEntity organizacion = obtenerOrganizacion(usuario);
        SolicitudAdopcionEntity solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la solicitud."));
        AnimalEntity animal = animalRepository.findByIdForUpdate(solicitud.getAnimal().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la mascota."));
        // Reload after acquiring the animal lock to serialize decisions for this pet.
        entityManager.refresh(solicitud, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!solicitud.getAnimal().getOrganizacion().getId().equals(organizacion.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar solicitudes de otra organización.");
        }
        if (solicitud.getEstado() != EstadoSolicitudAdopcion.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta solicitud ya fue atendida.");
        }
        if (request.estado() == EstadoSolicitudAdopcion.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige aprobar o rechazar la solicitud.");
        }
        solicitud.setEstado(request.estado());
        if (request.estado() == EstadoSolicitudAdopcion.APROBADA) {
            if (animal.getEstado() != EstadoAnimal.DISPONIBLE && animal.getEstado() != EstadoAnimal.EN_PROCESO) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta mascota ya no está disponible.");
            }
            animal.setEstado(EstadoAnimal.ADOPTADO);
            for (SolicitudAdopcionEntity otra : solicitudRepository.findByAnimal_IdAndEstado(
                    animal.getId(), EstadoSolicitudAdopcion.PENDIENTE)) {
                if (!otra.getId().equals(solicitud.getId())) otra.setEstado(EstadoSolicitudAdopcion.RECHAZADA);
            }
        }
        return convertir(solicitudRepository.save(solicitud));
    }

    private OrganizacionEntity obtenerOrganizacion(UsuarioEntity usuario) {
        validarUsuario(usuario);
        OrganizacionEntity organizacion = organizacionRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta no está vinculada a una organización."));
        if (!Boolean.TRUE.equals(organizacion.getActivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La organización debe estar activa.");
        }
        return organizacion;
    }

    private void validarUsuario(UsuarioEntity usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }
    }

    private String limpiarMensaje(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) return null;
        return mensaje.trim();
    }

    private SolicitudAdopcionResponse convertir(SolicitudAdopcionEntity solicitud) {
        UsuarioEntity adoptante = solicitud.getAdoptante();
        return new SolicitudAdopcionResponse(
                solicitud.getId(), solicitud.getAnimal().getId(), solicitud.getAnimal().getNombre(),
                adoptante.getName() + " " + adoptante.getLastName(), adoptante.getEmail(),
                solicitud.getMensaje(), solicitud.getEstado(), solicitud.getFechaSolicitud());
    }
}
