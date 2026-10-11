package com.hc.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.OrganizacionEntity;

public interface OrganizacionRepository
        extends JpaRepository<OrganizacionEntity, Long> {

    boolean existsByCorreo(String correo);

    Optional<OrganizacionEntity> findByUsuario_Id(Long usuarioId);
}