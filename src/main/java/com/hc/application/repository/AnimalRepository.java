package com.hc.application.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hc.application.enums.EstadoAnimal;

import com.hc.application.entity.AnimalEntity;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long>{
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from AnimalEntity a where a.id = :id")
    java.util.Optional<AnimalEntity> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    @Query("""
            select a from AnimalEntity a
            where a.estado = :estado and (
                :especie = 'TODAS'
                or (:especie = 'PERRO' and lower(trim(a.especie)) in ('perro', 'perros', 'canino'))
                or (:especie = 'GATO' and lower(trim(a.especie)) in ('gato', 'gatos', 'felino'))
                or (:especie = 'OTRO' and lower(trim(a.especie)) not in
                    ('perro', 'perros', 'canino', 'gato', 'gatos', 'felino'))
            )
            """)
    Page<AnimalEntity> buscarCatalogo(@Param("estado") EstadoAnimal estado,
            @Param("especie") String especie, Pageable pageable);

    List<AnimalEntity> findByOrganizacion_IdOrderByFechaPublicacionDesc(Long organizacionId);
}
