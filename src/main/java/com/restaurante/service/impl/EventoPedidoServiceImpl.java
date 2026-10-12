package com.restaurante.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.mapper.EventoPedidoMapper;
import com.restaurante.model.domain.EventoPedido;
import com.restaurante.persistence.document.EventoPedidoDocument;
import com.restaurante.persistence.repository.EventoPedidoMongoRepository;
import com.restaurante.service.IEventoPedidoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoPedidoServiceImpl implements IEventoPedidoService {

    private final EventoPedidoMongoRepository eventoRepository;
    private final EventoPedidoMapper eventoMapper;

    @Override
    public EventoPedido registrar(Long idPedido, String tipo, String descripcion) {
        EventoPedido evento = EventoPedido.builder()
                .idPedido(idPedido)
                .tipo(tipo)
                .descripcion(descripcion)
                .timestamp(LocalDateTime.now(ZoneId.of("America/Bogota")))
                .build();
        EventoPedidoDocument guardado = eventoRepository.save(eventoMapper.toDocument(evento));
        log.info("Evento registrado: idPedido={}, tipo={}", idPedido, tipo);
        return eventoMapper.toDomain(guardado);
    }

    @Override
    public List<EventoPedido> obtenerPorPedido(Long idPedido) {
        return eventoRepository.findByIdPedidoOrderByTimestampAsc(idPedido).stream()
                .map(eventoMapper::toDomain)
                .toList();
    }
}
