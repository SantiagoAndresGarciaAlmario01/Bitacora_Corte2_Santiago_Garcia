package com.restaurante.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.restaurante.model.domain.Plato;
import com.restaurante.service.IPlatoService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementacion de {@link IPlatoService}.
 *
 * <p>A diferencia de una version anterior que manejaba el {@code Map} y el
 * {@code AtomicLong} directamente en esta clase, aqui el almacenamiento y las
 * operaciones basicas de CRUD viven en {@link AbstractCrudServiceImpl}. Esta
 * clase solo se encarga de:</p>
 * <ul>
 *   <li>decirle a la clase base como identificar y generar ids para un Plato,</li>
 *   <li>traducir las operaciones del dominio (RF de platos) a las operaciones
 *       genericas heredadas (buscarPorId, guardar, filtrar, etc.),</li>
 *   <li>aplicar las reglas propias de Plato que la clase base no conoce
 *       (activar/desactivar disponibilidad, copiar campos al actualizar).</li>
 * </ul>
 */
@Service
@Slf4j
public class PlatoServiceImpl extends AbstractCrudServiceImpl<Plato> implements IPlatoService {

    private final AtomicLong secuenciaId = new AtomicLong(1);

    @Override
    protected Long obtenerId(Plato entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Plato entidad, Long id) {
        entidad.setId(id);
    }

    @Override
    protected Long generarSiguienteId() {
        return secuenciaId.getAndIncrement();
    }

    @Override
    protected String nombreRecurso() {
        return "Plato";
    }

    @Override
    public List<Plato> obtenerTodos() {
        List<Plato> platos = listarTodos();
        log.info("Obteniendo todos los platos. Total: {}", platos.size());
        return platos;
    }

    @Override
    public List<Plato> obtenerDisponibles() {
        return filtrar(Plato::estaDisponible);
    }

    @Override
    public List<Plato> obtenerPorCategoria(String categoria) {
        return filtrar(p -> p.getCategoria().equalsIgnoreCase(categoria));
    }

    @Override
    public Plato obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    @Override
    public Plato crear(Plato plato) {
        return guardar(plato);
    }

    @Override
    public Plato actualizar(Long id, Plato nuevosDatos) {
        Plato existente = buscarPorId(id);

        existente.setNombre(nuevosDatos.getNombre());
        existente.setPrecio(nuevosDatos.getPrecio());
        existente.setCategoria(nuevosDatos.getCategoria());
        existente.setDescripcion(nuevosDatos.getDescripcion());

        log.info("Plato actualizado: id={}", id);
        return existente;
    }

    @Override
    public Plato cambiarDisponibilidad(Long id, boolean disponible) {
        Plato plato = buscarPorId(id);
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
        eliminarPorId(id);
    }
}
