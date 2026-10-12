package com.restaurante.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.EventoPedido;
import com.restaurante.model.dto.response.EventoPedidoResponseDTO;
import com.restaurante.persistence.document.EventoPedidoDocument;

@Mapper(componentModel = "spring")
public interface EventoPedidoMapper {

    @Mapping(target = "id", ignore = true)
    EventoPedidoDocument toDocument(EventoPedido evento);

    EventoPedido toDomain(EventoPedidoDocument doc);

    EventoPedidoResponseDTO toResponse(EventoPedido evento);
}
