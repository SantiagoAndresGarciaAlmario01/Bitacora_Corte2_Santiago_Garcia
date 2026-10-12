package com.restaurante.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.MesaEntityMapper;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.persistence.entity.MesaEntity;
import com.restaurante.persistence.repository.MesaJpaRepository;
import com.restaurante.service.IMesaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MesaServiceImpl implements IMesaService {

    private final MesaJpaRepository mesaRepository;
    private final MesaEntityMapper entityMapper;

    @Override
    public List<Mesa> obtenerTodos() {
        List<MesaEntity> mesas = mesaRepository.findAll();
        log.info("Obteniendo todas las mesas. Total: {}", mesas.size());
        return mesas.stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public Mesa obtenerPorId(Long id) {
        return mesaRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("Mesa no encontrada: id={}", id);
                    return new RecursoNoEncontradoException("Mesa", id);
                });
    }

    @Override
    public Mesa crear(Mesa mesa) {
        MesaEntity guardada = mesaRepository.save(entityMapper.toEntity(mesa));
        log.info("Mesa creada: id={}, numero={}", guardada.getId(), guardada.getNumero());
        return entityMapper.toDomain(guardada);
    }

    @Override
    public Mesa actualizar(Long id, Mesa nuevosDatos) {
        MesaEntity existente = mesaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mesa", id));
        existente.setNumero(nuevosDatos.getNumero());
        existente.setCapacidad(nuevosDatos.getCapacidad());
        log.info("Mesa actualizada: id={}", id);
        return entityMapper.toDomain(mesaRepository.save(existente));
    }

    @Override
    public Mesa cambiarEstado(Long id, EstadoMesa estado) {
        MesaEntity existente = mesaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mesa", id));
        existente.setEstado(estado);
        log.info("Mesa id={} -> estado={}", id, estado);
        return entityMapper.toDomain(mesaRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!mesaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Mesa", id);
        }
        mesaRepository.deleteById(id);
        log.info("Mesa eliminada: id={}", id);
    }
}
