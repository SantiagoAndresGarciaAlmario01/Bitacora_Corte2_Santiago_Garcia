package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.exception.ReglaDeNegocioException;
import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.persistence.convertidor.CuentaConvertidor;
import com.restaurante.persistence.convertidor.PedidoConvertidor;
import com.restaurante.persistence.entity.CuentaEntity;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.persistence.repository.CuentaRepository;
import com.restaurante.persistence.repository.PedidoRepository;
import com.restaurante.service.IMesaService;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private IMesaService mesaService;

    private CuentaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CuentaServiceImpl(cuentaRepository, new CuentaConvertidor(),
                pedidoRepository, new PedidoConvertidor(), mesaService);
    }

    private CuentaEntity cuenta(Long id, EstadoCuenta estado) {
        return CuentaEntity.builder().id(id).idMesa(3L).estado(estado).total(0.0)
                .fechaApertura(LocalDateTime.now().minusHours(1)).build();
    }

    private PedidoEntity pedido(Long id, EstadoPedido estado, double precio, int cantidad) {
        PedidoEntity pedido = PedidoEntity.builder().id(id).idMesa(3L).idCuenta(1L)
                .estado(estado).fechaCreacion(LocalDateTime.now()).build();
        pedido.agregarItem(ItemPedidoEntity.builder().id(id * 10).idPlato(1L).nombrePlato("Plato")
                .precioUnitario(precio).cantidad(cantidad).build());
        return pedido;
    }

    private void guardarDevuelveLoMismo() {
        when(cuentaRepository.save(any(CuentaEntity.class))).thenAnswer(inv -> {
            CuentaEntity e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
    }

    @Test
    @DisplayName("abrir() crea la cuenta ABIERTA en cero y ocupa la mesa")
    void abrirCuentaOcupaMesa() {
        when(cuentaRepository.findFirstByIdMesaAndEstado(3L, EstadoCuenta.ABIERTA)).thenReturn(Optional.empty());
        guardarDevuelveLoMismo();

        Cuenta cuenta = service.abrir(3L);

        assertThat(cuenta.estaAbierta()).isTrue();
        assertThat(cuenta.getTotal()).isZero();
        assertThat(cuenta.getFechaApertura()).isNotNull();
        verify(mesaService).obtenerPorId(3L);
        verify(mesaService).cambiarEstado(3L, EstadoMesa.OCUPADA);
    }

    @Test
    @DisplayName("abrir() falla si la mesa ya tiene una cuenta abierta")
    void abrirConCuentaYaAbierta() {
        when(cuentaRepository.findFirstByIdMesaAndEstado(3L, EstadoCuenta.ABIERTA))
                .thenReturn(Optional.of(cuenta(1L, EstadoCuenta.ABIERTA)));

        assertThatThrownBy(() -> service.abrir(3L)).isInstanceOf(ConflictoException.class);
        verify(cuentaRepository, never()).save(any(CuentaEntity.class));
    }

    @Test
    @DisplayName("obtenerAbiertaDeMesa() falla si la mesa no tiene cuenta abierta")
    void obtenerAbiertaSinCuenta() {
        when(cuentaRepository.findFirstByIdMesaAndEstado(3L, EstadoCuenta.ABIERTA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerAbiertaDeMesa(3L)).isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    @DisplayName("calcularConsumoActual() suma los pedidos que no estan cancelados")
    void consumoActualIgnoraCancelados() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta(1L, EstadoCuenta.ABIERTA)));
        when(pedidoRepository.findByIdCuenta(1L)).thenReturn(List.of(
                pedido(1L, EstadoPedido.EN_PREPARACION, 20000.0, 2),
                pedido(2L, EstadoPedido.CANCELADO, 99000.0, 1)));

        assertThat(service.calcularConsumoActual(1L)).isEqualTo(40000.0);
    }

    @Test
    @DisplayName("cerrar() no deja cerrar si hay pedidos en curso")
    void cerrarConPedidosPendientes() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta(1L, EstadoCuenta.ABIERTA)));
        when(pedidoRepository.findByIdCuenta(1L)).thenReturn(List.of(pedido(1L, EstadoPedido.LISTO, 10000.0, 1)));

        assertThatThrownBy(() -> service.cerrar(1L)).isInstanceOf(ReglaDeNegocioException.class);
        verify(mesaService, never()).cambiarEstado(any(), any());
    }

    @Test
    @DisplayName("cerrar() cobra solo lo ENTREGADO y libera la mesa")
    void cerrarCobraEntregadosYLiberaMesa() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta(1L, EstadoCuenta.ABIERTA)));
        when(pedidoRepository.findByIdCuenta(1L)).thenReturn(List.of(
                pedido(1L, EstadoPedido.ENTREGADO, 32000.0, 2),
                pedido(2L, EstadoPedido.CANCELADO, 18000.0, 1)));
        when(cuentaRepository.existsById(1L)).thenReturn(true);
        guardarDevuelveLoMismo();

        Cuenta cerrada = service.cerrar(1L);

        assertThat(cerrada.getEstado()).isEqualTo(EstadoCuenta.CERRADA);
        assertThat(cerrada.getTotal()).isEqualTo(64000.0);
        assertThat(cerrada.getFechaCierre()).isNotNull();
        verify(mesaService).cambiarEstado(3L, EstadoMesa.LIBRE);
    }

    @Test
    @DisplayName("cerrar() una cuenta ya cerrada lanza EstadoInvalidoException")
    void cerrarCuentaYaCerrada() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta(1L, EstadoCuenta.CERRADA)));

        assertThatThrownBy(() -> service.cerrar(1L)).isInstanceOf(EstadoInvalidoException.class);
    }
}
