package com.hc.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.AnimalEntity;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long>{
    List<AnimalEntity> findByOrganizacion_IdOrderByFechaPublicacionDesc(Long organizacionId);
}
