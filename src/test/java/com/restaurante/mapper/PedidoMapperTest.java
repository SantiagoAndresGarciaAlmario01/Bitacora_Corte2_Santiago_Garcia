package com.restaurante.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.dto.request.PedidoRequestDTO;
import com.restaurante.model.dto.response.PedidoResponseDTO;

class PedidoMapperTest {

    private PedidoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PedidoMapperImpl(new ItemPedidoMapperImpl());
    }

    @Test
    @DisplayName("toDomain mapea idMesa, arranca en RECIBIDO y sin items")
    void toDomain_debeMapearCamposYForzarEstadoRecibido() {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setIdMesa(5L);

        Pedido pedido = mapper.toDomain(dto);

        assertNull(pedido.getId());
        assertEquals(5L, pedido.getIdMesa());
        assertEquals(EstadoPedido.RECIBIDO, pedido.getEstado());
        assertTrue(pedido.getItems() == null || pedido.getItems().isEmpty());
    }

    @Test
    @DisplayName("toResponse mapea los items y calcula el total")
    void toResponse_debeMapearItemsYCalcularTotal() {
        ItemPedido item1 = ItemPedido.builder().id(1L).idPedido(1L).idPlato(1L)
                .nombrePlato("Roll").precioUnitario(10000.0).cantidad(2).build();
        ItemPedido item2 = ItemPedido.builder().id(2L).idPedido(1L).idPlato(2L)
                .nombrePlato("Sashimi").precioUnitario(15000.0).cantidad(1).build();
        Pedido pedido = Pedido.builder()
                .id(1L)
                .idMesa(3L)
                .estado(EstadoPedido.RECIBIDO)
                .items(List.of(item1, item2))
                .build();

        PedidoResponseDTO response = mapper.toResponse(pedido);

        assertEquals(1L, response.getId());
        assertEquals(3L, response.getIdMesa());
        assertEquals(2, response.getItems().size());
        assertEquals(35000.0, response.getTotal());
    }

    @Test
    @DisplayName("toResponseList mapea cada elemento de la lista de dominio")
    void toResponseList_debeMapearCadaElemento() {
        Pedido pedido1 = Pedido.builder().id(1L).idMesa(1L).estado(EstadoPedido.RECIBIDO).build();
        Pedido pedido2 = Pedido.builder().id(2L).idMesa(2L).estado(EstadoPedido.LISTO).build();

        List<PedidoResponseDTO> resultado = mapper.toResponseList(List.of(pedido1, pedido2));

        assertEquals(2, resultado.size());
        assertEquals(EstadoPedido.LISTO, resultado.get(1).getEstado());
    }
}
