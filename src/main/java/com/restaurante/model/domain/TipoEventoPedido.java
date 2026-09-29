package com.restaurante.model.domain;

/**
 * Tipos de evento que quedan registrados en el historial de un pedido (MongoDB).
 */
public enum TipoEventoPedido {
    PEDIDO_CREADO,
    ITEM_AGREGADO,
    ITEM_RETIRADO,
    ESTADO_CAMBIADO,
    PEDIDO_ELIMINADO
}
