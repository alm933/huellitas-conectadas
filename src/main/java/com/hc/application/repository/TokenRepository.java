package com.hc.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hc.application.entity.TokenEntity;
import com.hc.application.entity.UsuarioEntity;

import java.util.Optional;
import java.util.List;



public interface TokenRepository extends JpaRepository<TokenEntity,Long>{

    Optional<TokenEntity> findByToken(String token);

    List<TokenEntity> findAllByUserAndExpiredFalseAndRevokedFalse(UsuarioEntity user);
}
