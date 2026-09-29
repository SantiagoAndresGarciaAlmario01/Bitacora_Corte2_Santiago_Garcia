package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.persistence.entity.RegistroVehiculoEntity;

@Repository
public interface RegistroVehiculoRepository extends JpaRepository<RegistroVehiculoEntity, Long> {

    List<RegistroVehiculoEntity> findByHoraSalidaIsNull();

    long countByHoraSalidaIsNull();

    boolean existsByPlacaAndHoraSalidaIsNull(String placa);
}
