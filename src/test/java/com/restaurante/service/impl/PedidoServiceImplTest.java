package com.restaurante.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PedidoEntityMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.persistence.repository.PedidoJpaRepository;
import com.restaurante.service.IEventoPedidoService;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoJpaRepository pedidoRepository;

    @Mock
    private PedidoEntityMapper entityMapper;

    @Mock
    private IEventoPedidoService eventoPedidoService;

    @InjectMocks
    private PedidoServiceImpl service;

    private Pedido pedidoDominio(Long id, Long idMesa, EstadoPedido estado) {
        return Pedido.builder().id(id).idMesa(idMesa).estado(estado).build();
    }

    private PedidoEntity pedidoEntity(Long id, Long idMesa, EstadoPedido estado) {
        return PedidoEntity.builder().id(id).idMesa(idMesa).estado(estado).build();
    }

    @Test
    @DisplayName("obtenerTodos con la lista vacia devuelve lista vacia, no null")
    void obtenerTodos_listaVacia_debeRetornarListaVacia() {
        when(pedidoRepository.findAll()).thenReturn(List.of());

        List<Pedido> resultado = service.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("crear asigna un id, guarda el pedido y registra el evento PEDIDO_CREADO")
    void crear_debeAsignarIdYGuardar() {
        Pedido nuevo = pedidoDominio(null, 1L, EstadoPedido.RECIBIDO);
        PedidoEntity entitySinId = pedidoEntity(null, 1L, EstadoPedido.RECIBIDO);
        PedidoEntity entityGuardada = pedidoEntity(1L, 1L, EstadoPedido.RECIBIDO);
        when(entityMapper.toEntity(nuevo)).thenReturn(entitySinId);
        when(pedidoRepository.save(entitySinId)).thenReturn(entityGuardada);
        when(entityMapper.toDomain(entityGuardada)).thenReturn(pedidoDominio(1L, 1L, EstadoPedido.RECIBIDO));

        Pedido resultado = service.crear(nuevo);

        assertNotNull(resultado.getId());
        verify(eventoPedidoService, times(1)).registrar(eq(1L), eq("PEDIDO_CREADO"), anyString());
    }

    @Test
    @DisplayName("obtenerPorId con id inexistente lanza RecursoNoEncontradoException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(999L));
    }

    @Test
    @DisplayName("obtenerPorMesa filtra los pedidos de esa mesa")
    void obtenerPorMesa_debeFiltrarPorMesa() {
        PedidoEntity entity = pedidoEntity(2L, 2L, EstadoPedido.RECIBIDO);
        when(pedidoRepository.findByIdMesa(2L)).thenReturn(List.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(pedidoDominio(2L, 2L, EstadoPedido.RECIBIDO));

        List<Pedido> resultado = service.obtenerPorMesa(2L);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getIdMesa());
    }

    @Test
    @DisplayName("actualizar cambia la mesa asociada al pedido")
    void actualizar_existente_debeActualizarCampos() {
        PedidoEntity existente = pedidoEntity(1L, 1L, EstadoPedido.RECIBIDO);
        Pedido nuevosDatos = pedidoDominio(null, 2L, EstadoPedido.RECIBIDO);
        PedidoEntity actualizada = pedidoEntity(1L, 2L, EstadoPedido.RECIBIDO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(pedidoRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(pedidoDominio(1L, 2L, EstadoPedido.RECIBIDO));

        Pedido resultado = service.actualizar(1L, nuevosDatos);

        assertEquals(2L, resultado.getIdMesa());
    }

    @Test
    @DisplayName("actualizar con id inexistente lanza RecursoNoEncontradoException")
    void actualizar_inexistente_debeLanzarExcepcion() {
        Pedido nuevosDatos = pedidoDominio(null, 1L, EstadoPedido.RECIBIDO);
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(999L, nuevosDatos));
    }

    @Test
    @DisplayName("cambiarEstado actualiza el estado del pedido y registra el evento CAMBIO_ESTADO")
    void cambiarEstado_debeActualizarEstado() {
        PedidoEntity existente = pedidoEntity(1L, 1L, EstadoPedido.RECIBIDO);
        PedidoEntity actualizada = pedidoEntity(1L, 1L, EstadoPedido.EN_PREPARACION);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(pedidoRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(pedidoDominio(1L, 1L, EstadoPedido.EN_PREPARACION));

        Pedido resultado = service.cambiarEstado(1L, EstadoPedido.EN_PREPARACION);

        assertEquals(EstadoPedido.EN_PREPARACION, resultado.getEstado());
        verify(eventoPedidoService, times(1)).registrar(eq(1L), eq("CAMBIO_ESTADO"), anyString());
    }

    @Test
    @DisplayName("cambiarEstado con id inexistente lanza RecursoNoEncontradoException")
    void cambiarEstado_inexistente_debeLanzarExcepcion() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cambiarEstado(999L, EstadoPedido.LISTO));
        verify(eventoPedidoService, never()).registrar(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("eliminar con id existente elimina el pedido")
    void eliminar_existente_debeEliminarPedido() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar con id inexistente lanza RecursoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(pedidoRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(999L));
        verify(pedidoRepository, never()).deleteById(anyLong());
    }
}
