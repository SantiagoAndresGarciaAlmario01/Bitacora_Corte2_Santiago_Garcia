package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;

public interface IPedidoService {

    List<Pedido> obtenerTodos();

    List<Pedido> obtenerPorMesa(Long idMesa);

    List<Pedido> obtenerPorEstado(EstadoPedido estado);

    Pedido obtenerPorId(Long id);

    Pedido crear(Long idMesa);

    Pedido agregarItem(Long idPedido, Long idPlato, int cantidad);

    Pedido retirarItem(Long idPedido, Long idItem);

    Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado);

    void eliminar(Long id);
}
