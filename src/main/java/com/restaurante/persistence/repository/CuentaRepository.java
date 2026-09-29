package com.restaurante.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.persistence.entity.CuentaEntity;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {

    Optional<CuentaEntity> findFirstByIdMesaAndEstado(Long idMesa, EstadoCuenta estado);

    List<CuentaEntity> findByEstado(EstadoCuenta estado);
}
