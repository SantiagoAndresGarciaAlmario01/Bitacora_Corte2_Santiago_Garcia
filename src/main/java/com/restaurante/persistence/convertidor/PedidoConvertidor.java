package com.restaurante.persistence.convertidor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.entity.PedidoEntity;

/**
 * Convierte el pedido junto con sus items. El id del pedido de cada item se
 * toma del padre (y no de item.getPedido()) para no disparar cargas perezosas.
 */
@Component
public class PedidoConvertidor implements ConvertidorEntidad<Pedido, PedidoEntity> {

    @Override
    public Pedido aDominio(PedidoEntity entidad) {
        List<ItemPedido> items = entidad.getItems().stream()
                .map(item -> itemADominio(item, entidad.getId()))
                .toList();
        Pedido pedido = Pedido.builder()
                .id(entidad.getId())
                .idMesa(entidad.getIdMesa())
                .idCuenta(entidad.getIdCuenta())
                .estado(entidad.getEstado())
                .fechaCreacion(entidad.getFechaCreacion())
                .build();
        items.forEach(pedido::agregarItem);
        return pedido;
    }

    @Override
    public PedidoEntity aEntidad(Pedido dominio) {
        PedidoEntity entidad = PedidoEntity.builder()
                .id(dominio.getId())
                .idMesa(dominio.getIdMesa())
                .idCuenta(dominio.getIdCuenta())
                .estado(dominio.getEstado())
                .fechaCreacion(dominio.getFechaCreacion())
                .build();
        if (dominio.getItems() != null) {
            dominio.getItems().forEach(item -> entidad.agregarItem(itemAEntidad(item)));
        }
        return entidad;
    }

    public ItemPedido itemADominio(ItemPedidoEntity item, Long idPedido) {
        return ItemPedido.builder()
                .id(item.getId())
                .idPedido(idPedido)
                .idPlato(item.getIdPlato())
                .nombrePlato(item.getNombrePlato())
                .precioUnitario(item.getPrecioUnitario())
                .cantidad(item.getCantidad())
                .build();
    }

    public ItemPedidoEntity itemAEntidad(ItemPedido item) {
        return ItemPedidoEntity.builder()
                .id(item.getId())
                .idPlato(item.getIdPlato())
                .nombrePlato(item.getNombrePlato())
                .precioUnitario(item.getPrecioUnitario())
                .cantidad(item.getCantidad())
                .build();
    }
}
