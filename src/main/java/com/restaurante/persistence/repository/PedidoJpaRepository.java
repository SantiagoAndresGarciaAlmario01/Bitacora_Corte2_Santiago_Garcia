package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.persistence.entity.PedidoEntity;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {

    List<PedidoEntity> findByIdMesa(Long idMesa);
}
