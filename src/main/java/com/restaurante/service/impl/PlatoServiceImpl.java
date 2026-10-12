package com.restaurante.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PlatoEntityMapper;
import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.PlatoEntity;
import com.restaurante.persistence.repository.PlatoJpaRepository;
import com.restaurante.service.IPlatoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatoServiceImpl implements IPlatoService {

    private final PlatoJpaRepository platoRepository;
    private final PlatoEntityMapper entityMapper;

    @Override
    public List<Plato> obtenerTodos() {
        List<PlatoEntity> platos = platoRepository.findAll();
        log.info("Obteniendo todos los platos. Total: {}", platos.size());
        return platos.stream().map(entityMapper::toDomain).toList();
    }

    @Override
    public List<Plato> obtenerDisponibles() {
        return platoRepository.findByDisponible(true).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Plato> obtenerPorCategoria(String categoria) {
        return platoRepository.findByCategoriaIgnoreCase(categoria).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public Plato obtenerPorId(Long id) {
        return platoRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("Plato no encontrado: id={}", id);
                    return new RecursoNoEncontradoException("Plato", id);
                });
    }

    @Override
    public Plato crear(Plato plato) {
        if (platoRepository.existsByNombreIgnoreCase(plato.getNombre())) {
            throw new ConflictoException("Ya existe un plato con el nombre '" + plato.getNombre() + "'");
        }
        PlatoEntity guardado = platoRepository.save(entityMapper.toEntity(plato));
        log.info("Plato creado: id={}, nombre={}", guardado.getId(), guardado.getNombre());
        return entityMapper.toDomain(guardado);
    }

    @Override
    @Transactional
    public Plato actualizar(Long id, Plato nuevosDatos) {
        PlatoEntity existente = platoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plato", id));
        if (platoRepository.existsByNombreIgnoreCaseAndIdNot(nuevosDatos.getNombre(), id)) {
            throw new ConflictoException("Ya existe otro plato con el nombre '" + nuevosDatos.getNombre() + "'");
        }
        existente.setNombre(nuevosDatos.getNombre());
        existente.setPrecio(nuevosDatos.getPrecio());
        existente.setCategoria(nuevosDatos.getCategoria());
        existente.setDescripcion(nuevosDatos.getDescripcion());
        log.info("Plato actualizado: id={}", id);
        return entityMapper.toDomain(platoRepository.save(existente));
    }

    @Override
    public Plato cambiarDisponibilidad(Long id, boolean disponible) {
        PlatoEntity existente = platoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plato", id));
        existente.setDisponible(disponible);
        log.info("Plato id={} -> disponible={}", id, disponible);
        return entityMapper.toDomain(platoRepository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!platoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Plato", id);
        }
        platoRepository.deleteById(id);
        log.info("Plato eliminado: id={}", id);
    }
}
