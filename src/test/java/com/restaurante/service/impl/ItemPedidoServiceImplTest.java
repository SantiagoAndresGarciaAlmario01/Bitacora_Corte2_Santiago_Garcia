package com.restaurante.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.restaurante.mapper.ItemPedidoEntityMapper;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.repository.ItemPedidoJpaRepository;
import com.restaurante.service.IPedidoService;
import com.restaurante.service.IPlatoService;

@ExtendWith(MockitoExtension.class)
class ItemPedidoServiceImplTest {

    @Mock
    private ItemPedidoJpaRepository itemRepository;

    @Mock
    private ItemPedidoEntityMapper entityMapper;

    @Mock
    private IPedidoService pedidoService;

    @Mock
    private IPlatoService platoService;

    @InjectMocks
    private ItemPedidoServiceImpl service;

    private static final Long ID_PEDIDO = 1L;
    private static final Long ID_PLATO = 1L;

    private Pedido pedidoDePrueba() {
        return Pedido.builder().id(ID_PEDIDO).idMesa(1L).build();
    }

    private Plato platoDePrueba() {
        return Plato.builder().id(ID_PLATO).nombre("Roll California").precio(25000.0)
                .categoria("Roll").disponible(true).build();
    }

    private ItemPedido itemDominio(Long id, int cantidad, String nombrePlato, Double precio) {
        return ItemPedido.builder().id(id).idPedido(ID_PEDIDO).idPlato(ID_PLATO)
                .nombrePlato(nombrePlato).precioUnitario(precio).cantidad(cantidad).build();
    }

    private ItemPedidoEntity itemEntity(Long id, int cantidad, String nombrePlato, Double precio) {
        return ItemPedidoEntity.builder().id(id).idPedido(ID_PEDIDO).idPlato(ID_PLATO)
                .nombrePlato(nombrePlato).precioUnitario(precio).cantidad(cantidad).build();
    }

    @Test
    @DisplayName("obtenerTodos con la lista vacia devuelve lista vacia, no null")
    void obtenerTodos_listaVacia_debeRetornarListaVacia() {
        when(itemRepository.findAll()).thenReturn(List.of());

        List<ItemPedido> resultado = service.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("crear congela el nombre y el precio del plato")
    void crear_debeCongelarNombreYPrecio() {
        ItemPedido nuevo = ItemPedido.builder().idPedido(ID_PEDIDO).idPlato(ID_PLATO).cantidad(2).build();
        ItemPedidoEntity entitySinId = itemEntity(null, 2, "Roll California", 25000.0);
        ItemPedidoEntity entityGuardada = itemEntity(1L, 2, "Roll California", 25000.0);
        when(pedidoService.obtenerPorId(ID_PEDIDO)).thenReturn(pedidoDePrueba());
        when(platoService.obtenerPorId(ID_PLATO)).thenReturn(platoDePrueba());
        when(entityMapper.toEntity(nuevo)).thenReturn(entitySinId);
        when(itemRepository.save(entitySinId)).thenReturn(entityGuardada);
        when(entityMapper.toDomain(entityGuardada)).thenReturn(itemDominio(1L, 2, "Roll California", 25000.0));

        ItemPedido resultado = service.crear(nuevo);

        assertNotNull(resultado.getId());
        assertEquals("Roll California", resultado.getNombrePlato());
        assertEquals(25000.0, resultado.getPrecioUnitario());
    }

    @Test
    @DisplayName("crear con idPedido inexistente lanza RecursoNoEncontradoException")
    void crear_pedidoInexistente_debeLanzarExcepcion() {
        ItemPedido item = ItemPedido.builder().idPedido(999L).idPlato(ID_PLATO).cantidad(1).build();
        when(pedidoService.obtenerPorId(999L)).thenThrow(new RecursoNoEncontradoException("Pedido", 999L));

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(item));
        verify(itemRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("crear con idPlato inexistente lanza RecursoNoEncontradoException")
    void crear_platoInexistente_debeLanzarExcepcion() {
        ItemPedido item = ItemPedido.builder().idPedido(ID_PEDIDO).idPlato(999L).cantidad(1).build();
        when(pedidoService.obtenerPorId(ID_PEDIDO)).thenReturn(pedidoDePrueba());
        when(platoService.obtenerPorId(999L)).thenThrow(new RecursoNoEncontradoException("Plato", 999L));

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(item));
        verify(itemRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("obtenerPorPedido filtra los items de ese pedido")
    void obtenerPorPedido_debeFiltrarPorPedido() {
        ItemPedidoEntity entity = itemEntity(1L, 1, "Roll California", 25000.0);
        when(itemRepository.findByIdPedido(ID_PEDIDO)).thenReturn(List.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(itemDominio(1L, 1, "Roll California", 25000.0));

        List<ItemPedido> resultado = service.obtenerPorPedido(ID_PEDIDO);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("actualizarCantidad cambia la cantidad del item")
    void actualizarCantidad_debeActualizarCantidad() {
        ItemPedidoEntity existente = itemEntity(1L, 1, "Roll California", 25000.0);
        ItemPedidoEntity actualizado = itemEntity(1L, 4, "Roll California", 25000.0);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(itemRepository.save(existente)).thenReturn(actualizado);
        when(entityMapper.toDomain(actualizado)).thenReturn(itemDominio(1L, 4, "Roll California", 25000.0));

        ItemPedido resultado = service.actualizarCantidad(1L, 4);

        assertEquals(4, resultado.getCantidad());
    }

    @Test
    @DisplayName("actualizarCantidad con id inexistente lanza RecursoNoEncontradoException")
    void actualizarCantidad_inexistente_debeLanzarExcepcion() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizarCantidad(999L, 2));
    }

    @Test
    @DisplayName("eliminar con id existente elimina el item")
    void eliminar_existente_debeEliminarItem() {
        when(itemRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar con id inexistente lanza RecursoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(itemRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(999L));
        verify(itemRepository, never()).deleteById(anyLong());
    }
}
