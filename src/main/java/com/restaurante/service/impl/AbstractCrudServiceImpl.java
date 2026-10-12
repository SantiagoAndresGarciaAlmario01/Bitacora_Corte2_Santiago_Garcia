package com.restaurante.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.restaurante.exception.RecursoNoEncontradoException;

import lombok.extern.slf4j.Slf4j;

/**
 * Base generica para servicios que necesitan un CRUD en memoria mientras el
 * proyecto no tiene persistencia real (ver README: "Estado actual: sin
 * persistencia").
 *
 * <p>Centraliza aqui el almacenamiento (Map), la generacion de ids y las
 * operaciones basicas (listar, buscar, guardar, reemplazar, eliminar,
 * filtrar) para que cada servicio concreto (por ejemplo {@code PlatoServiceImpl})
 * solo tenga que resolver el "como" de su propio dominio: como se obtiene el
 * id de la entidad, como se le asigna un id nuevo y como se llama el recurso
 * para los mensajes de error.</p>
 *
 * @param <T> tipo de la entidad de dominio administrada (ej. Plato)
 */
@Slf4j
public abstract class AbstractCrudServiceImpl<T> {

    private final Map<Long, T> almacen = new ConcurrentHashMap<>();

    /**
     * Extrae el id actual de la entidad (puede ser null si aun no se ha guardado).
     */
    protected abstract Long obtenerId(T entidad);

    /**
     * Asigna un id ya generado a la entidad.
     */
    protected abstract void asignarId(T entidad, Long id);

    /**
     * Calcula el siguiente id disponible para una entidad nueva.
     */
    protected abstract Long generarSiguienteId();

    /**
     * Nombre legible del recurso, usado en los mensajes de
     * {@link RecursoNoEncontradoException} (ej. "Plato").
     */
    protected abstract String nombreRecurso();

    protected List<T> listarTodos() {
        return List.copyOf(almacen.values());
    }

    protected List<T> filtrar(Predicate<T> criterio) {
        return almacen.values().stream()
                .filter(criterio)
                .toList();
    }

    protected T buscarPorId(Long id) {
        return Optional.ofNullable(almacen.get(id))
                .orElseThrow(() -> {
                    log.warn("{} no encontrado: id={}", nombreRecurso(), id);
                    return new RecursoNoEncontradoException(nombreRecurso(), id);
                });
    }

    protected boolean existe(Long id) {
        return almacen.containsKey(id);
    }

    protected T guardar(T entidad) {
        Long id = generarSiguienteId();
        asignarId(entidad, id);
        almacen.put(id, entidad);
        log.info("{} guardado: id={}", nombreRecurso(), id);
        return entidad;
    }

    protected T reemplazar(Long id, T entidad) {
        buscarPorId(id);
        if (!id.equals(obtenerId(entidad))) {
            asignarId(entidad, id);
        }
        almacen.put(id, entidad);
        log.info("{} reemplazado: id={}", nombreRecurso(), id);
        return entidad;
    }

    protected void eliminarPorId(Long id) {
        buscarPorId(id);
        almacen.remove(id);
        log.info("{} eliminado: id={}", nombreRecurso(), id);
    }
}
