package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.persistence.entity.ReservaEntity;

public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByIdMesa(Long idMesa);
}
