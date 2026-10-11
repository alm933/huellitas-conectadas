package com.hc.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.AnimalFotoEntity;

public interface AnimalFotoRepository
        extends JpaRepository<AnimalFotoEntity, Long> {

    List<AnimalFotoEntity> findByAnimal_IdOrderByIdAsc(Long animalId);
}