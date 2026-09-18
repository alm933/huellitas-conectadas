package com.hc.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.AnimalEntity;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long>{

}
