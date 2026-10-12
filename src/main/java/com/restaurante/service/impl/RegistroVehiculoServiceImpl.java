package com.restaurante.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.RegistroVehiculoEntityMapper;
import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.persistence.entity.RegistroVehiculoEntity;
import com.restaurante.persistence.repository.RegistroVehiculoJpaRepository;
import com.restaurante.service.IRegistroVehiculoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroVehiculoServiceImpl implements IRegistroVehiculoService {

    private final RegistroVehiculoJpaRepository registroRepository;
    private final RegistroVehiculoEntityMapper entityMapper;

    @Override
    public List<RegistroVehiculo> obtenerTodos() {
        List<RegistroVehiculoEntity> registros = registroRepository.findAll();
        log.info("Obteniendo todos los registros de vehiculo. Total: {}", registros.size());
        return registros.stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public List<RegistroVehiculo> obtenerActivos() {
        return registroRepository.findByHoraSalidaIsNull().stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public RegistroVehiculo obtenerPorId(Long id) {
        return registroRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("Registro de vehiculo no encontrado: id={}", id);
                    return new RecursoNoEncontradoException("RegistroVehiculo", id);
                });
    }

    @Override
    public RegistroVehiculo crear(RegistroVehiculo registro) {
        RegistroVehiculoEntity guardado = registroRepository.save(entityMapper.toEntity(registro));
        log.info("Registro de vehiculo creado: id={}, placa={}", guardado.getId(), guardado.getPlaca());
        return entityMapper.toDomain(guardado);
    }

    @Override
    public RegistroVehiculo registrarSalida(Long id) {
        RegistroVehiculoEntity existente = registroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("RegistroVehiculo", id));
        existente.setHoraSalida(LocalDateTime.now(ZoneId.of("America/Bogota")));
        log.info("Registro de vehiculo id={} -> salida registrada", id);
        return entityMapper.toDomain(registroRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!registroRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("RegistroVehiculo", id);
        }
        registroRepository.deleteById(id);
        log.info("Registro de vehiculo eliminado: id={}", id);
    }
}
