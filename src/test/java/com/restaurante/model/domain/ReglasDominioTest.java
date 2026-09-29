package com.restaurante.model.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReglasDominioTest {

    @Test
    @DisplayName("Pedido.calcularTotal() suma cantidad x precio de cada item")
    void totalDelPedido() {
        Pedido pedido = Pedido.builder().estado(EstadoPedido.RECIBIDO).build();
        pedido.agregarItem(ItemPedido.builder().precioUnitario(32000.0).cantidad(2).build());
        pedido.agregarItem(ItemPedido.builder().precioUnitario(7000.0).cantidad(3).build());

        assertThat(pedido.calcularTotal()).isEqualTo(85000.0);
    }

    @Test
    @DisplayName("Pedido: solo se cobra lo ENTREGADO y solo se modifica antes de LISTO")
    void reglasDeCobroYModificacion() {
        Pedido entregado = Pedido.builder().estado(EstadoPedido.ENTREGADO).items(List.of()).build();
        Pedido enCocina = Pedido.builder().estado(EstadoPedido.EN_PREPARACION).build();

        assertThat(entregado.cuentaParaCobro()).isTrue();
        assertThat(entregado.estaCerrado()).isTrue();
        assertThat(entregado.puedeModificarse()).isFalse();
        assertThat(enCocina.cuentaParaCobro()).isFalse();
        assertThat(enCocina.puedeModificarse()).isTrue();
    }

    @Test
    @DisplayName("ItemPedido.subtotal() es 0 si falta el precio o la cantidad")
    void subtotalIncompleto() {
        assertThat(ItemPedido.builder().cantidad(2).build().subtotal()).isZero();
    }

    @Test
    @DisplayName("Mesa.alcanzaPara() compara contra la capacidad")
    void capacidadDeMesa() {
        Mesa mesa = Mesa.builder().capacidad(4).build();

        assertThat(mesa.alcanzaPara(4)).isTrue();
        assertThat(mesa.alcanzaPara(5)).isFalse();
    }

    @Test
    @DisplayName("Reserva.chocaCon() detecta reservas a menos del margen, antes o despues")
    void cruceDeReservas() {
        LocalDateTime base = LocalDateTime.of(2030, 1, 10, 19, 0);
        Reserva reserva = Reserva.builder().fechaHora(base).build();

        assertThat(reserva.chocaCon(base.plusMinutes(119), 2)).isTrue();
        assertThat(reserva.chocaCon(base.minusMinutes(90), 2)).isTrue();
        assertThat(reserva.chocaCon(base.plusHours(2), 2)).isFalse();
        assertThat(reserva.chocaCon(null, 2)).isFalse();
    }

    @Test
    @DisplayName("Cuenta.cerrar() fija el total y la fecha de cierre")
    void cierreDeCuenta() {
        Cuenta cuenta = Cuenta.builder().idMesa(1L).build();
        cuenta.abrir();

        cuenta.cerrar(50000.0);

        assertThat(cuenta.estaAbierta()).isFalse();
        assertThat(cuenta.getTotal()).isEqualTo(50000.0);
        assertThat(cuenta.getFechaCierre()).isNotNull();
    }

    @Test
    @DisplayName("RegistroVehiculo.minutosEstacionado() usa la hora de salida si ya salio")
    void minutosEstacionado() {
        LocalDateTime entrada = LocalDateTime.of(2030, 1, 10, 12, 0);
        RegistroVehiculo registro = RegistroVehiculo.builder()
                .horaEntrada(entrada).horaSalida(entrada.plusMinutes(95)).build();

        assertThat(registro.minutosEstacionado()).isEqualTo(95);
        assertThat(registro.estaActivo()).isFalse();
    }
}
