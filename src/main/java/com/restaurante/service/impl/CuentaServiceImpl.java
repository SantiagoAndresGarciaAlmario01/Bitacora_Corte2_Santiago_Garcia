package com.restaurante.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.CuentaEntityMapper;
import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.persistence.entity.CuentaEntity;
import com.restaurante.persistence.repository.CuentaJpaRepository;
import com.restaurante.service.ICuentaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaServiceImpl implements ICuentaService {

    private final CuentaJpaRepository cuentaRepository;
    private final CuentaEntityMapper entityMapper;

    @Override
    public List<Cuenta> obtenerTodos() {
        List<CuentaEntity> cuentas = cuentaRepository.findAll();
        log.info("Obteniendo todas las cuentas. Total: {}", cuentas.size());
        return cuentas.stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public List<Cuenta> obtenerPorMesa(Long idMesa) {
        return cuentaRepository.findByIdMesa(idMesa).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public Cuenta obtenerPorId(Long id) {
        return cuentaRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("Cuenta no encontrada: id={}", id);
                    return new RecursoNoEncontradoException("Cuenta", id);
                });
    }

    @Override
    public Cuenta crear(Cuenta cuenta) {
        CuentaEntity guardada = cuentaRepository.save(entityMapper.toEntity(cuenta));
        log.info("Cuenta creada: id={}, idMesa={}", guardada.getId(), guardada.getIdMesa());
        return entityMapper.toDomain(guardada);
    }

    @Override
    public Cuenta actualizarTotal(Long id, Double total) {
        CuentaEntity existente = cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta", id));
        existente.setTotal(total);
        log.info("Cuenta id={} -> total={}", id, total);
        return entityMapper.toDomain(cuentaRepository.save(existente));
    }

    @Override
    public Cuenta cerrar(Long id) {
        CuentaEntity existente = cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta", id));
        existente.setEstado(EstadoCuenta.CERRADA);
        log.info("Cuenta cerrada: id={}", id);
        return entityMapper.toDomain(cuentaRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!cuentaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cuenta", id);
        }
        cuentaRepository.deleteById(id);
        log.info("Cuenta eliminada: id={}", id);
    }
}
