package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.persistence.entity.CuentaEntity;

public interface CuentaJpaRepository extends JpaRepository<CuentaEntity, Long> {

    List<CuentaEntity> findByIdMesa(Long idMesa);
}
