package com.hc.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.AnimalEntity;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long>{
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from AnimalEntity a where a.id = :id")
    java.util.Optional<AnimalEntity> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    List<AnimalEntity> findByOrganizacion_IdOrderByFechaPublicacionDesc(Long organizacionId);
}
