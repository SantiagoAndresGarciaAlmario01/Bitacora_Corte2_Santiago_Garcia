package com.restaurante.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.dto.request.ItemPedidoRequestDTO;
import com.restaurante.model.dto.response.ItemPedidoResponseDTO;

class ItemPedidoMapperTest {

    private ItemPedidoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ItemPedidoMapperImpl();
    }

    @Test
    @DisplayName("toDomain mapea idPedido, idPlato y cantidad, dejando nombre y precio sin asignar")
    void toDomain_debeMapearCamposBasicos() {
        ItemPedidoRequestDTO dto = new ItemPedidoRequestDTO();
        dto.setIdPedido(1L);
        dto.setIdPlato(2L);
        dto.setCantidad(3);

        ItemPedido item = mapper.toDomain(dto);

        assertNull(item.getId());
        assertEquals(1L, item.getIdPedido());
        assertEquals(2L, item.getIdPlato());
        assertEquals(3, item.getCantidad());
        assertNull(item.getNombrePlato());
        assertNull(item.getPrecioUnitario());
    }

    @Test
    @DisplayName("toResponse mapea todos los campos e incluye el subtotal calculado")
    void toResponse_debeMapearCamposYCalcularSubtotal() {
        ItemPedido item = ItemPedido.builder()
                .id(1L)
                .idPedido(10L)
                .idPlato(20L)
                .nombrePlato("Roll California")
                .precioUnitario(25000.0)
                .cantidad(2)
                .build();

        ItemPedidoResponseDTO response = mapper.toResponse(item);

        assertEquals(1L, response.getId());
        assertEquals("Roll California", response.getNombrePlato());
        assertEquals(50000.0, response.getSubtotal());
    }

    @Test
    @DisplayName("toResponseList mapea cada elemento de la lista de dominio")
    void toResponseList_debeMapearCadaElemento() {
        ItemPedido item1 = ItemPedido.builder().id(1L).idPedido(1L).idPlato(1L)
                .nombrePlato("Roll").precioUnitario(10000.0).cantidad(1).build();
        ItemPedido item2 = ItemPedido.builder().id(2L).idPedido(1L).idPlato(2L)
                .nombrePlato("Sashimi").precioUnitario(20000.0).cantidad(2).build();

        List<ItemPedidoResponseDTO> resultado = mapper.toResponseList(List.of(item1, item2));

        assertEquals(2, resultado.size());
        assertEquals(10000.0, resultado.get(0).getSubtotal());
        assertEquals(40000.0, resultado.get(1).getSubtotal());
    }
}
