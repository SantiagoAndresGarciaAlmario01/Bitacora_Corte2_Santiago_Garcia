package com.restaurante.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.Plato;
import com.restaurante.service.IPlatoService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlatoServiceImpl implements IPlatoService {

    private final Map<Long, Plato> platos = new ConcurrentHashMap<>();
    private final AtomicLong contador = new AtomicLong(1);

    @Override
    public List<Plato> obtenerTodos() {
        log.info("Obteniendo todos los platos. Total: {}", platos.size());
        return platos.values().stream().toList();
    }

    @Override
    public List<Plato> obtenerDisponibles() {
        return platos.values().stream()
                .filter(Plato::estaDisponible)
                .toList();
    }

    @Override
    public List<Plato> obtenerPorCategoria(String categoria) {
        return platos.values().stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase(categoria))
                .toList();
    }

    @Override
    public Plato obtenerPorId(Long id) {
        return platos.values().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Plato no encontrado: id={}", id);
                    return new RecursoNoEncontradoException("Plato", id);
                });
    }

    @Override
    public Plato crear(Plato plato) {
        plato.setId(contador.getAndIncrement());
        platos.put(plato.getId(), plato);
        log.info("Plato creado: id={}, nombre={}", plato.getId(), plato.getNombre());
        return plato;
    }

    @Override
    public Plato actualizar(Long id, Plato nuevosDatos) {
        Plato existente = obtenerPorId(id);

        existente.setNombre(nuevosDatos.getNombre());
        existente.setPrecio(nuevosDatos.getPrecio());
        existente.setCategoria(nuevosDatos.getCategoria());
        existente.setDescripcion(nuevosDatos.getDescripcion());
        log.info("Plato actualizado: id={}", id);
        return existente;
    }

    @Override
    public Plato cambiarDisponibilidad(Long id, boolean disponible) {
        Plato plato = obtenerPorId(id);
        if (disponible) {
            plato.activar();
        } else {
            plato.desactivar();
        }
        log.info("Plato id={} -> disponible={}", id, disponible);
        return plato;
    }

    @Override
    public void eliminar(Long id) {
        obtenerPorId(id);
        platos.remove(id);
        log.info("Plato eliminado: id={}", id);
    }
}
