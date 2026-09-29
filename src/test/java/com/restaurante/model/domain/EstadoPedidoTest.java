package com.restaurante.model.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EstadoPedidoTest {

    @ParameterizedTest(name = "{0} -> {1} permitido={2}")
    @CsvSource({
            "RECIBIDO, EN_PREPARACION, true",
            "RECIBIDO, CANCELADO, true",
            "RECIBIDO, LISTO, false",
            "RECIBIDO, ENTREGADO, false",
            "EN_PREPARACION, LISTO, true",
            "EN_PREPARACION, CANCELADO, true",
            "EN_PREPARACION, RECIBIDO, false",
            "LISTO, ENTREGADO, true",
            "LISTO, CANCELADO, false",
            "ENTREGADO, CANCELADO, false",
            "CANCELADO, RECIBIDO, false"
    })
    @DisplayName("las transiciones siguen el flujo de cocina")
    void transiciones(EstadoPedido desde, EstadoPedido hacia, boolean permitido) {
        assertThat(desde.puedeCambiarA(hacia)).isEqualTo(permitido);
    }

    @Test
    @DisplayName("ENTREGADO y CANCELADO son estados finales")
    void estadosFinales() {
        assertThat(EstadoPedido.ENTREGADO.esFinal()).isTrue();
        assertThat(EstadoPedido.CANCELADO.esFinal()).isTrue();
        assertThat(EstadoPedido.LISTO.esFinal()).isFalse();
    }

    @Test
    @DisplayName("cambiar a null nunca esta permitido")
    void cambiarANull() {
        assertThat(EstadoPedido.RECIBIDO.puedeCambiarA(null)).isFalse();
    }
}
