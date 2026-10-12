package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.persistence.entity.PlatoEntity;

public interface PlatoJpaRepository extends JpaRepository<PlatoEntity, Long> {

    List<PlatoEntity> findByCategoriaIgnoreCase(String categoria);
    List<PlatoEntity> findByDisponible(Boolean disponible);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

}
