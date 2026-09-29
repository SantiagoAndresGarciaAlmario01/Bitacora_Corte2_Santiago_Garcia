package com.restaurante.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.persistence.entity.ReservaEntity;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByIdMesaAndCanceladaFalse(Long idMesa);

    List<ReservaEntity> findByCanceladaFalseAndFechaHoraAfterOrderByFechaHoraAsc(LocalDateTime desde);
}
