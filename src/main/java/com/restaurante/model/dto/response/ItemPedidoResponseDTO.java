package com.restaurante.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedidoResponseDTO {

    private Long id;
    private Long idPedido;
    private Long idPlato;
    private String nombrePlato;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}
