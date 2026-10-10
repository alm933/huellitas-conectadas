package com.hc.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hc.application.dto.animal.AnimalFotoResponse;
import com.hc.application.dto.animal.AnimalPublicacionResponse;
import com.hc.application.dto.animal.AnimalRequest;
import com.hc.application.dto.animal.AnimalResponse;
import com.hc.application.entity.AnimalEntity;
import com.hc.application.entity.AnimalFotoEntity;
import com.hc.application.entity.OrganizacionEntity;
import com.hc.application.entity.UsuarioEntity;
import com.hc.application.enums.EstadoAnimal;
import com.hc.application.repository.AnimalFotoRepository;
import com.hc.application.repository.AnimalRepository;
import com.hc.application.repository.OrganizacionRepository;

@Service
public class AnimalPublicacionService {

    private static final long MAX_BYTES_POR_FOTO = 5 * 1024 * 1024;

    private final AnimalRepository animalRepository;
    private final AnimalFotoRepository fotoRepository;
    private final OrganizacionRepository organizacionRepository;
    private final GarageStorageService garageStorageService;

    public AnimalPublicacionService(
            AnimalRepository animalRepository,
            AnimalFotoRepository fotoRepository,
            OrganizacionRepository organizacionRepository,
            GarageStorageService garageStorageService) {
        this.animalRepository = animalRepository;
        this.fotoRepository = fotoRepository;
        this.organizacionRepository = organizacionRepository;
        this.garageStorageService = garageStorageService;
    }

    @Transactional
    public AnimalPublicacionResponse publicar(
            AnimalRequest request,
            List<MultipartFile> archivos,
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
                    "La organización debe estar activa para publicar mascotas.");
        }

        if (archivos == null || archivos.size() < 2 || archivos.size() > 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes adjuntar entre 2 y 8 fotos.");
        }

        List<String> extensiones = archivos.stream()
                .map(this::validarFoto)
                .toList();

        AnimalEntity animal = new AnimalEntity();
        animal.setNombre(request.getNombre());
        animal.setEspecie(request.getEspecie());
        animal.setRaza(request.getRaza());
        animal.setEdadMeses(request.getEdadMeses());
        animal.setSexo(request.getSexo());
        animal.setTamano(request.getTamanio());
        animal.setDescripcion(request.getDescripcion());
        animal.setEstado(request.getEstado() == null
                ? EstadoAnimal.DISPONIBLE
                : request.getEstado());
        animal.setOrganizacion(organizacion);

        List<String> clavesSubidas = new ArrayList<>();

        try {
            animal = animalRepository.save(animal);
            List<AnimalFotoEntity> fotos = new ArrayList<>();

            for (int i = 0; i < archivos.size(); i++) {
                MultipartFile archivo = archivos.get(i);
                String key = "animales/" + animal.getId() + "/"
                        + UUID.randomUUID() + extensiones.get(i);

                garageStorageService.subir(key, archivo);
                clavesSubidas.add(key);

                AnimalFotoEntity foto = new AnimalFotoEntity();
                foto.setAnimal(animal);
                foto.setGarageKey(key);
                foto.setContentType(archivo.getContentType());
                foto.setSizeBytes(archivo.getSize());
                fotos.add(foto);
            }

            fotos = fotoRepository.saveAll(fotos);

            animal.setFotoUrl("/api/v1/fotos/" + fotos.get(0).getId());
            animalRepository.save(animal);

            AnimalResponse animalResponse = new AnimalResponse(
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
                    organizacion.getId(),
                    organizacion.getNombre());

            List<AnimalFotoResponse> fotosResponse = fotos.stream()
                    .map(foto -> new AnimalFotoResponse(
                            foto.getId(),
                            "/api/v1/fotos/" + foto.getId(),
                            foto.getContentType()))
                    .toList();

            return new AnimalPublicacionResponse(animalResponse, fotosResponse);
        } catch (IOException e) {
            limpiarGarage(clavesSubidas);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudieron subir las fotos a Garage.", e);
        } catch (RuntimeException e) {
            limpiarGarage(clavesSubidas);
            throw e;
        }
    }

    private String validarFoto(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()
                || archivo.getSize() > MAX_BYTES_POR_FOTO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cada foto debe pesar más de 0 y como máximo 5 MB.");
        }

        try (InputStream input = archivo.getInputStream()) {
            byte[] header = input.readNBytes(12);
            String tipo = archivo.getContentType();

            if ("image/jpeg".equals(tipo)
                    && header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF) {
                return ".jpg";
            }

            if ("image/png".equals(tipo)
                    && header.length >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 'P'
                    && header[2] == 'N'
                    && header[3] == 'G') {
                return ".png";
            }

            if ("image/webp".equals(tipo)
                    && header.length >= 12
                    && new String(header, 0, 4).equals("RIFF")
                    && new String(header, 8, 4).equals("WEBP")) {
                return ".webp";
            }
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No se pudo leer una foto.", e);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Solo se aceptan imágenes JPEG, PNG o WebP válidas.");
    }

    private void limpiarGarage(List<String> claves) {
        for (String key : claves) {
            try {
                garageStorageService.eliminar(key);
            } catch (RuntimeException ignored) {
                // La limpieza no debe ocultar el error que causó el fallo.
            }
        }
    }
}