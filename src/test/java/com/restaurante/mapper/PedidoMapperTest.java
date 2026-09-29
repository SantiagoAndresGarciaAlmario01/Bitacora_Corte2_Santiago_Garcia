package com.restaurante.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.dto.response.PedidoResponseDTO;

class PedidoMapperTest {

    private final PedidoMapper mapper = Mappers.getMapper(PedidoMapper.class);

    @Test
    @DisplayName("toResponse() incluye items con subtotal y el total del pedido")
    void toResponseCalculaSubtotalesYTotal() {
        Pedido pedido = Pedido.builder().id(1L).idMesa(3L).idCuenta(10L).estado(EstadoPedido.RECIBIDO).build();
        pedido.agregarItem(ItemPedido.builder().id(5L).idPlato(2L).nombrePlato("Temaki Spicy Tuna")
                .precioUnitario(24000.0).cantidad(2).build());

        PedidoResponseDTO dto = mapper.toResponse(pedido);

        assertThat(dto.getIdCuenta()).isEqualTo(10L);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getSubtotal()).isEqualTo(48000.0);
        assertThat(dto.getTotal()).isEqualTo(48000.0);
    }
}
