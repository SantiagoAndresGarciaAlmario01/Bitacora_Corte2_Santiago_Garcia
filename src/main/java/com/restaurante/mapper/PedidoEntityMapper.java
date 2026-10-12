package com.restaurante.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.Pedido;
import com.restaurante.persistence.entity.PedidoEntity;

@Mapper(componentModel = "spring")
public interface PedidoEntityMapper {

    PedidoEntity toEntity(Pedido pedido);

    @Mapping(target = "items", ignore = true)
    Pedido toDomain(PedidoEntity entity);
}
