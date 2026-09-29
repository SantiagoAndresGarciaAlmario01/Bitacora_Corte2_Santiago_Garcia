package com.restaurante.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.convertidor.ConvertidorEntidad;
import com.restaurante.persistence.convertidor.PlatoConvertidor;
import com.restaurante.persistence.entity.PlatoEntity;
import com.restaurante.persistence.repository.PlatoRepository;
import com.restaurante.service.IPlatoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementacion de {@link IPlatoService} sobre PostgreSQL.
 *
 * <p>El contrato publico es el mismo de la Semana 8; lo unico que cambio es
 * que los datos ahora viven en la tabla {@code platos} y las consultas de
 * menu (disponibles / por categoria) las resuelve la base de datos con
 * metodos derivados de Spring Data en vez de filtrar un {@code Map}.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PlatoServiceImpl extends AbstractCrudServiceImpl<Plato, PlatoEntity> implements IPlatoService {

    private final PlatoRepository platoRepository;
    private final PlatoConvertidor platoConvertidor;

    @Override
    protected JpaRepository<PlatoEntity, Long> repositorio() {
        return platoRepository;
    }

    @Override
    protected ConvertidorEntidad<Plato, PlatoEntity> convertidor() {
        return platoConvertidor;
    }

    @Override
    protected void asignarId(Plato dominio, Long id) {
        dominio.setId(id);
    }

    @Override
    protected String nombreRecurso() {
        return "Plato";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> obtenerTodos() {
        List<Plato> platos = listarTodos();
        log.info("Obteniendo todos los platos. Total: {}", platos.size());
        return platos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> obtenerDisponibles() {
        return aDominio(platoRepository.findByDisponibleTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> obtenerPorCategoria(String categoria) {
        return aDominio(platoRepository.findByCategoriaIgnoreCase(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public Plato obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    @Override
    public Plato crear(Plato plato) {
        if (plato.getDisponible() == null) {
            plato.activar();
        }
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
        return reemplazar(id, existente);
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
        return reemplazar(id, plato);
    }

    @Override
    public void eliminar(Long id) {
        eliminarPorId(id);
    }
}
