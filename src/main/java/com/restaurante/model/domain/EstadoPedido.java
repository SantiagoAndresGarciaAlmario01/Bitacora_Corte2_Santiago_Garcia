package com.restaurante.model.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de un pedido en cocina.
 *
 * <pre>
 * RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO
 *     \____________\__________-> CANCELADO
 * </pre>
 *
 * Un pedido solo puede cancelarse mientras no haya salido de cocina
 * (RECIBIDO o EN_PREPARACION). ENTREGADO y CANCELADO son estados finales.
 */
public enum EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    public Set<EstadoPedido> siguientesPermitidos() {
        return switch (this) {
            case RECIBIDO -> EnumSet.of(EN_PREPARACION, CANCELADO);
            case EN_PREPARACION -> EnumSet.of(LISTO, CANCELADO);
            case LISTO -> EnumSet.of(ENTREGADO);
            case ENTREGADO, CANCELADO -> EnumSet.noneOf(EstadoPedido.class);
        };
    }

    public boolean puedeCambiarA(EstadoPedido nuevo) {
        return nuevo != null && siguientesPermitidos().contains(nuevo);
    }

    public boolean esFinal() {
        return siguientesPermitidos().isEmpty();
    }
}
