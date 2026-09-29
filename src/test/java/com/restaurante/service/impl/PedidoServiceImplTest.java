package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.exception.ReglaDeNegocioException;
import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.Plato;
import com.restaurante.model.domain.TipoEventoPedido;
import com.restaurante.persistence.convertidor.PedidoConvertidor;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.persistence.repository.PedidoRepository;
import com.restaurante.service.ICuentaService;
import com.restaurante.service.IHistorialPedidoService;
import com.restaurante.service.IPlatoService;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ICuentaService cuentaService;
    @Mock
    private IPlatoService platoService;
    @Mock
    private IHistorialPedidoService historialService;

    private PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PedidoServiceImpl(pedidoRepository, new PedidoConvertidor(),
                cuentaService, platoService, historialService);
    }

    private PedidoEntity pedido(EstadoPedido estado, boolean conItem) {
        PedidoEntity pedido = PedidoEntity.builder().id(1L).idMesa(3L).idCuenta(10L)
                .estado(estado).fechaCreacion(LocalDateTime.now()).build();
        if (conItem) {
            pedido.agregarItem(ItemPedidoEntity.builder().id(7L).idPlato(2L).nombrePlato("Sashimi Mixto")
                    .precioUnitario(38000.0).cantidad(1).build());
        }
        return pedido;
    }

    private Plato plato(boolean disponible) {
        return Plato.builder().id(2L).nombre("Roll Acevichado").precio(32000.0)
                .categoria("Roll").disponible(disponible).build();
    }

    @Test
    @DisplayName("crear() asocia el pedido a la cuenta abierta de la mesa y lo deja RECIBIDO")
    void crearPedido() {
        when(cuentaService.obtenerAbiertaDeMesa(3L))
                .thenReturn(Cuenta.builder().id(10L).idMesa(3L).estado(EstadoCuenta.ABIERTA).build());
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> {
            PedidoEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        Pedido creado = service.crear(3L);

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getIdCuenta()).isEqualTo(10L);
        assertThat(creado.getEstado()).isEqualTo(EstadoPedido.RECIBIDO);
        verify(historialService).registrar(eq(1L), eq(TipoEventoPedido.PEDIDO_CREADO), isNull(),
                eq(EstadoPedido.RECIBIDO), anyString());
    }

    @Test
    @DisplayName("crear() sin cuenta abierta no guarda nada")
    void crearSinCuentaAbierta() {
        when(cuentaService.obtenerAbiertaDeMesa(3L)).thenThrow(new ReglaDeNegocioException("sin cuenta"));

        assertThatThrownBy(() -> service.crear(3L)).isInstanceOf(ReglaDeNegocioException.class);
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
        verifyNoInteractions(historialService);
    }

    @Test
    @DisplayName("agregarItem() copia nombre y precio del plato y recalcula el total")
    void agregarItemDisponible() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, false)));
        when(platoService.obtenerPorId(2L)).thenReturn(plato(true));
        when(pedidoRepository.saveAndFlush(any(PedidoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido pedido = service.agregarItem(1L, 2L, 3);

        assertThat(pedido.getItems()).hasSize(1);
        assertThat(pedido.getItems().get(0).getNombrePlato()).isEqualTo("Roll Acevichado");
        assertThat(pedido.calcularTotal()).isEqualTo(96000.0);
        verify(historialService).registrar(eq(1L), eq(TipoEventoPedido.ITEM_AGREGADO), any(), any(), anyString());
    }

    @Test
    @DisplayName("agregarItem() rechaza platos no disponibles")
    void agregarItemNoDisponible() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, false)));
        when(platoService.obtenerPorId(2L)).thenReturn(plato(false));

        assertThatThrownBy(() -> service.agregarItem(1L, 2L, 1)).isInstanceOf(ReglaDeNegocioException.class);
        verify(pedidoRepository, never()).saveAndFlush(any(PedidoEntity.class));
    }

    @Test
    @DisplayName("agregarItem() no deja modificar un pedido que ya salio de cocina")
    void agregarItemPedidoListo() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.LISTO, true)));

        assertThatThrownBy(() -> service.agregarItem(1L, 2L, 1)).isInstanceOf(EstadoInvalidoException.class);
        verifyNoInteractions(platoService);
    }

    @Test
    @DisplayName("retirarItem() quita el item del pedido")
    void retirarItemExistente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, true)));
        when(pedidoRepository.saveAndFlush(any(PedidoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido pedido = service.retirarItem(1L, 7L);

        assertThat(pedido.getItems()).isEmpty();
    }

    @Test
    @DisplayName("retirarItem() con un item que no existe lanza RecursoNoEncontradoException")
    void retirarItemInexistente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, true)));

        assertThatThrownBy(() -> service.retirarItem(1L, 99L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("cambiarEstado() sigue el flujo de cocina y lo registra en el historial")
    void cambiarEstadoValido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, true)));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido pedido = service.cambiarEstado(1L, EstadoPedido.EN_PREPARACION);

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.EN_PREPARACION);
        verify(historialService).registrar(eq(1L), eq(TipoEventoPedido.ESTADO_CAMBIADO),
                eq(EstadoPedido.RECIBIDO), eq(EstadoPedido.EN_PREPARACION), anyString());
    }

    @Test
    @DisplayName("cambiarEstado() rechaza saltos que no respetan el flujo")
    void cambiarEstadoInvalido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, true)));

        assertThatThrownBy(() -> service.cambiarEstado(1L, EstadoPedido.ENTREGADO))
                .isInstanceOf(EstadoInvalidoException.class)
                .hasMessageContaining("RECIBIDO");
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    @DisplayName("cambiarEstado() no manda a cocina un pedido vacio")
    void cambiarEstadoPedidoVacio() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, false)));

        assertThatThrownBy(() -> service.cambiarEstado(1L, EstadoPedido.EN_PREPARACION))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    @DisplayName("eliminar() no borra pedidos que ya estan en cocina o entregados")
    void eliminarPedidoEnCocina() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.LISTO, true)));

        assertThatThrownBy(() -> service.eliminar(1L)).isInstanceOf(EstadoInvalidoException.class);
        verify(pedidoRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar() borra un pedido RECIBIDO")
    void eliminarPedidoRecibido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido(EstadoPedido.RECIBIDO, false)));
        when(pedidoRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(pedidoRepository).deleteById(1L);
    }
}
