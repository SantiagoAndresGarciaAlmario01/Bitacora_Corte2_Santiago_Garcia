package com.restaurante.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.persistence.entity.MesaEntity;

public interface MesaJpaRepository extends JpaRepository<MesaEntity, Long> {
}
