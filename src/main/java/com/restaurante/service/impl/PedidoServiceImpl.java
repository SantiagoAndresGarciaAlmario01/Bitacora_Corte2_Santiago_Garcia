package com.restaurante.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PedidoEntityMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.persistence.repository.PedidoJpaRepository;
import com.restaurante.service.IEventoPedidoService;
import com.restaurante.service.IPedidoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoJpaRepository pedidoRepository;
    private final PedidoEntityMapper entityMapper;
    private final IEventoPedidoService eventoPedidoService;

    @Override
    public List<Pedido> obtenerTodos() {
        List<PedidoEntity> pedidos = pedidoRepository.findAll();
        log.info("Obteniendo todos los pedidos. Total: {}", pedidos.size());
        return pedidos.stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public List<Pedido> obtenerPorMesa(Long idMesa) {
        return pedidoRepository.findByIdMesa(idMesa).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("Pedido no encontrado: id={}", id);
                    return new RecursoNoEncontradoException("Pedido", id);
                });
    }

    @Override
    public Pedido crear(Pedido pedido) {
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.RECIBIDO);
        }
        PedidoEntity guardado = pedidoRepository.save(entityMapper.toEntity(pedido));
        log.info("Pedido creado: id={}, idMesa={}", guardado.getId(), guardado.getIdMesa());
        eventoPedidoService.registrar(guardado.getId(), "PEDIDO_CREADO",
                "Pedido creado para la mesa " + guardado.getIdMesa());
        return entityMapper.toDomain(guardado);
    }

    @Override
    public Pedido actualizar(Long id, Pedido nuevosDatos) {
        PedidoEntity existente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        existente.setIdMesa(nuevosDatos.getIdMesa());
        log.info("Pedido actualizado: id={}", id);
        return entityMapper.toDomain(pedidoRepository.save(existente));
    }

    @Override
    public Pedido cambiarEstado(Long id, EstadoPedido estado) {
        PedidoEntity existente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        existente.setEstado(estado);
        log.info("Pedido id={} -> estado={}", id, estado);
        PedidoEntity guardado = pedidoRepository.save(existente);
        eventoPedidoService.registrar(id, "CAMBIO_ESTADO", "Pedido " + id + " paso a estado " + estado);
        return entityMapper.toDomain(guardado);
    }

    @Override
    public void eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Pedido", id);
        }
        pedidoRepository.deleteById(id);
        log.info("Pedido eliminado: id={}", id);
    }
}
