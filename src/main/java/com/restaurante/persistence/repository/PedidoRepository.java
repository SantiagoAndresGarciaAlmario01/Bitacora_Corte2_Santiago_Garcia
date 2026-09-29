package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.persistence.entity.PedidoEntity;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    List<PedidoEntity> findByIdMesaOrderByFechaCreacionAsc(Long idMesa);

    List<PedidoEntity> findByIdCuenta(Long idCuenta);

    List<PedidoEntity> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);
}
