package com.restaurante.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.ReservaEntityMapper;
import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.entity.ReservaEntity;
import com.restaurante.persistence.repository.ReservaJpaRepository;
import com.restaurante.service.IReservaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaServiceImpl implements IReservaService {

    private final ReservaJpaRepository reservaRepository;
    private final ReservaEntityMapper entityMapper;

    @Override
    public List<Reserva> obtenerTodos() {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        log.info("Obteniendo todas las reservas. Total: {}", reservas.size());
        return reservas.stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public List<Reserva> obtenerPorMesa(Long idMesa) {
        return reservaRepository.findByIdMesa(idMesa).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("Reserva no encontrada: id={}", id);
                    return new RecursoNoEncontradoException("Reserva", id);
                });
    }

    @Override
    public Reserva crear(Reserva reserva) {
        ReservaEntity guardada = reservaRepository.save(entityMapper.toEntity(reserva));
        log.info("Reserva creada: id={}, idMesa={}", guardada.getId(), guardada.getIdMesa());
        return entityMapper.toDomain(guardada);
    }

    @Override
    public Reserva actualizar(Long id, Reserva nuevosDatos) {
        ReservaEntity existente = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
        existente.setNombreCliente(nuevosDatos.getNombreCliente());
        existente.setFechaHora(nuevosDatos.getFechaHora());
        existente.setNumeroPersonas(nuevosDatos.getNumeroPersonas());
        log.info("Reserva actualizada: id={}", id);
        return entityMapper.toDomain(reservaRepository.save(existente));
    }

    @Override
    public Reserva cancelar(Long id) {
        ReservaEntity existente = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
        existente.setCancelada(true);
        log.info("Reserva cancelada: id={}", id);
        return entityMapper.toDomain(reservaRepository.save(existente));
    }

    @Override
    public Reserva reprogramar(Long id, LocalDateTime nuevaFechaHora) {
        ReservaEntity existente = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
        existente.setFechaHora(nuevaFechaHora);
        log.info("Reserva id={} reprogramada a {}", id, nuevaFechaHora);
        return entityMapper.toDomain(reservaRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!reservaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Reserva", id);
        }
        reservaRepository.deleteById(id);
        log.info("Reserva eliminada: id={}", id);
    }
}
