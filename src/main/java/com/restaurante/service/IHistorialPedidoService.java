package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoEventoPedido;
import com.restaurante.persistence.document.HistorialPedidoDocument;

/**
 * Bitacora de todo lo que le pasa a un pedido, guardada en MongoDB.
 */
public interface IHistorialPedidoService {

    void registrar(Long idPedido, TipoEventoPedido tipo, EstadoPedido estadoAnterior,
                   EstadoPedido estadoNuevo, String detalle);

    List<HistorialPedidoDocument> obtenerPorPedido(Long idPedido);
}
