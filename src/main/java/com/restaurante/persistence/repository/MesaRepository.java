package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.persistence.entity.MesaEntity;

@Repository
public interface MesaRepository extends JpaRepository<MesaEntity, Long> {

    boolean existsByNumero(Integer numero);

    boolean existsByNumeroAndIdNot(Integer numero, Long id);

    List<MesaEntity> findByEstado(EstadoMesa estado);
}
