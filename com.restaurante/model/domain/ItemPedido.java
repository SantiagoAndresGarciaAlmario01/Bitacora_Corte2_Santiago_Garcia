package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido {

    private Long id;
    private Long idPedido;
    private Long idPlato;
    private String nombrePlato;
    private Double precioUnitario;
    private Integer cantidad;

    public double subtotal() {
        if (precioUnitario == null || cantidad == null) {
            return 0.0;
        }
        return precioUnitario * cantidad;
    }
}
