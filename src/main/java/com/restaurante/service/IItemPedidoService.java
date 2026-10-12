package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.ItemPedido;

public interface IItemPedidoService {

    List<ItemPedido> obtenerTodos();

    List<ItemPedido> obtenerPorPedido(Long idPedido);

    ItemPedido obtenerPorId(Long id);

    ItemPedido crear(ItemPedido item);

    ItemPedido actualizarCantidad(Long id, Integer cantidad);

    void eliminar(Long id);
}
