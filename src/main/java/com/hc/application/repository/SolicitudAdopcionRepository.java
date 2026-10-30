package com.hc.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.SolicitudAdopcionEntity;
import com.hc.application.enums.EstadoSolicitudAdopcion;

public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcionEntity, Long> {
    boolean existsByAnimal_IdAndAdoptante_IdAndEstado(
            Long animalId, Long adoptanteId, EstadoSolicitudAdopcion estado);

    List<SolicitudAdopcionEntity> findByAnimal_Organizacion_IdOrderByFechaSolicitudDesc(Long organizacionId);
}
