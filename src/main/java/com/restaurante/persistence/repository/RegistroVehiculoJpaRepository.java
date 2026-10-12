package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.persistence.entity.RegistroVehiculoEntity;

public interface RegistroVehiculoJpaRepository extends JpaRepository<RegistroVehiculoEntity, Long> {

    List<RegistroVehiculoEntity> findByHoraSalidaIsNull();
}
