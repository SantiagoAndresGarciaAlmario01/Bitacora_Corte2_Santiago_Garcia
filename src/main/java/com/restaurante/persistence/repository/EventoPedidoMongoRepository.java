package com.restaurante.persistence.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.restaurante.persistence.document.EventoPedidoDocument;

public interface EventoPedidoMongoRepository extends MongoRepository<EventoPedidoDocument, String> {

    List<EventoPedidoDocument> findByIdPedidoOrderByTimestampAsc(Long idPedido);
}
