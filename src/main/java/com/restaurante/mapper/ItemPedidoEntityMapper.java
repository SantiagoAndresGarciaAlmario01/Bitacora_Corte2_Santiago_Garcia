package com.restaurante.mapper;

import org.mapstruct.Mapper;

import com.restaurante.model.domain.ItemPedido;
import com.restaurante.persistence.entity.ItemPedidoEntity;

@Mapper(componentModel = "spring")
public interface ItemPedidoEntityMapper {

    ItemPedidoEntity toEntity(ItemPedido item);

    ItemPedido toDomain(ItemPedidoEntity entity);
}
