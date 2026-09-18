package com.hc.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.UsuarioEntity;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<UsuarioEntity,Long>{

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
