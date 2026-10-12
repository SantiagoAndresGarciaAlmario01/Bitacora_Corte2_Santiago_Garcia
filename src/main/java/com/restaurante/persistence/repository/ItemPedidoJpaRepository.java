package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.persistence.entity.ItemPedidoEntity;

public interface ItemPedidoJpaRepository extends JpaRepository<ItemPedidoEntity, Long> {

    List<ItemPedidoEntity> findByIdPedido(Long idPedido);
}
