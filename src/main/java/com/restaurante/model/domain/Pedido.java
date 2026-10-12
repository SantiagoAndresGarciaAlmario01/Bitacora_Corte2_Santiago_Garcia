package com.restaurante.model.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    private Long id;
    private Long idMesa;
    @Builder.Default
    private List<ItemPedido> items = new ArrayList<>();
    private EstadoPedido estado;

    public void agregarItem(ItemPedido item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public double calcularTotal() {
        if (items == null) {
            return 0.0;
        }
        return items.stream().mapToDouble(ItemPedido::subtotal).sum();
    }

    public boolean puedeModificarse() {
        return estado == EstadoPedido.RECIBIDO || estado == EstadoPedido.EN_PREPARACION;
    }
}
