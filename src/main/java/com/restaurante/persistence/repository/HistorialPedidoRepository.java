package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.restaurante.persistence.document.HistorialPedidoDocument;

@Repository
public interface HistorialPedidoRepository extends MongoRepository<HistorialPedidoDocument, String> {

    List<HistorialPedidoDocument> findByIdPedidoOrderByFechaAsc(Long idPedido);
}
