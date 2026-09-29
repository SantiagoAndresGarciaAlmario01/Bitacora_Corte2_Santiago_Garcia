package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.persistence.entity.PlatoEntity;

@Repository
public interface PlatoRepository extends JpaRepository<PlatoEntity, Long> {

    List<PlatoEntity> findByDisponibleTrue();

    List<PlatoEntity> findByCategoriaIgnoreCase(String categoria);
}
