package com.restaurante.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.restaurante.model.domain.EstadoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {

    private Long id;
    private Long idMesa;
    private Long idCuenta;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private List<ItemPedidoResponseDTO> items;
    private Double total;
}
