package com.restaurante.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.ItemPedidoEntityMapper;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.ItemPedidoEntity;
import com.restaurante.persistence.repository.ItemPedidoJpaRepository;
import com.restaurante.service.IItemPedidoService;
import com.restaurante.service.IPedidoService;
import com.restaurante.service.IPlatoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemPedidoServiceImpl implements IItemPedidoService {

    private final ItemPedidoJpaRepository itemRepository;
    private final ItemPedidoEntityMapper entityMapper;

    private final IPedidoService pedidoService;
    private final IPlatoService platoService;

    @Override
    public List<ItemPedido> obtenerTodos() {
        List<ItemPedidoEntity> items = itemRepository.findAll();
        log.info("Obteniendo todos los items de pedido. Total: {}", items.size());
        return items.stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public List<ItemPedido> obtenerPorPedido(Long idPedido) {
        return itemRepository.findByIdPedido(idPedido).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public ItemPedido obtenerPorId(Long id) {
        return itemRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("Item de pedido no encontrado: id={}", id);
                    return new RecursoNoEncontradoException("ItemPedido", id);
                });
    }

    @Override
    public ItemPedido crear(ItemPedido item) {
        Pedido pedido = pedidoService.obtenerPorId(item.getIdPedido());
        Plato plato = platoService.obtenerPorId(item.getIdPlato());

        item.setIdPedido(pedido.getId());
        item.setIdPlato(plato.getId());
        item.setNombrePlato(plato.getNombre());
        item.setPrecioUnitario(plato.getPrecio());

        ItemPedidoEntity guardado = itemRepository.save(entityMapper.toEntity(item));
        log.info("Item de pedido creado: id={}, idPedido={}, idPlato={}",
                guardado.getId(), guardado.getIdPedido(), guardado.getIdPlato());
        return entityMapper.toDomain(guardado);
    }

    @Override
    public ItemPedido actualizarCantidad(Long id, Integer cantidad) {
        ItemPedidoEntity existente = itemRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ItemPedido", id));
        existente.setCantidad(cantidad);
        log.info("Item de pedido id={} -> cantidad={}", id, cantidad);
        return entityMapper.toDomain(itemRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("ItemPedido", id);
        }
        itemRepository.deleteById(id);
        log.info("Item de pedido eliminado: id={}", id);
    }
}
