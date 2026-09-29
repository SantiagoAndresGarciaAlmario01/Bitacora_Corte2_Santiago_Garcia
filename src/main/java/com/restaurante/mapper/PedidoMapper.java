package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.dto.response.ItemPedidoResponseDTO;
import com.restaurante.model.dto.response.PedidoResponseDTO;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "total", expression = "java(pedido.calcularTotal())")
    PedidoResponseDTO toResponse(Pedido pedido);

    @Mapping(target = "subtotal", expression = "java(item.subtotal())")
    ItemPedidoResponseDTO toItemResponse(ItemPedido item);

    List<PedidoResponseDTO> toResponseList(List<Pedido> pedidos);
}
