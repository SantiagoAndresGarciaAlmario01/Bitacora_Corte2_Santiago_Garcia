package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.EventoPedido;

public interface IEventoPedidoService {

    EventoPedido registrar(Long idPedido, String tipo, String descripcion);

    List<EventoPedido> obtenerPorPedido(Long idPedido);
}
