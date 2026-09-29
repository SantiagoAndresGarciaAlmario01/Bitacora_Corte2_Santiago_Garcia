package com.restaurante.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.persistence.convertidor.ConvertidorEntidad;
import com.restaurante.persistence.convertidor.MesaConvertidor;
import com.restaurante.persistence.entity.MesaEntity;
import com.restaurante.persistence.repository.MesaRepository;
import com.restaurante.service.IMesaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reglas de mesas:
 * <ul>
 *   <li>No pueden existir dos mesas con el mismo numero.</li>
 *   <li>Toda mesa nueva arranca LIBRE.</li>
 *   <li>Una mesa OCUPADA no se puede eliminar ni cambiar de capacidad.</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MesaServiceImpl extends AbstractCrudServiceImpl<Mesa, MesaEntity> implements IMesaService {

    private final MesaRepository mesaRepository;
    private final MesaConvertidor mesaConvertidor;

    @Override
    protected JpaRepository<MesaEntity, Long> repositorio() {
        return mesaRepository;
    }

    @Override
    protected ConvertidorEntidad<Mesa, MesaEntity> convertidor() {
        return mesaConvertidor;
    }

    @Override
    protected void asignarId(Mesa dominio, Long id) {
        dominio.setId(id);
    }

    @Override
    protected String nombreRecurso() {
        return "Mesa";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mesa> obtenerTodas() {
        return listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mesa> obtenerPorEstado(EstadoMesa estado) {
        return aDominio(mesaRepository.findByEstado(estado));
    }

    @Override
    @Transactional(readOnly = true)
    public Mesa obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    @Override
    public Mesa crear(Mesa mesa) {
        if (mesaRepository.existsByNumero(mesa.getNumero())) {
            throw new ConflictoException("Ya existe una mesa con el numero " + mesa.getNumero());
        }
        mesa.liberar();
        return guardar(mesa);
    }

    @Override
    public Mesa actualizar(Long id, Mesa nuevosDatos) {
        Mesa existente = buscarPorId(id);
        if (mesaRepository.existsByNumeroAndIdNot(nuevosDatos.getNumero(), id)) {
            throw new ConflictoException("Ya existe otra mesa con el numero " + nuevosDatos.getNumero());
        }
        if (existente.estaOcupada() && !existente.getCapacidad().equals(nuevosDatos.getCapacidad())) {
            throw new EstadoInvalidoException("No se puede cambiar la capacidad de una mesa ocupada");
        }
        existente.setNumero(nuevosDatos.getNumero());
        existente.setCapacidad(nuevosDatos.getCapacidad());
        return reemplazar(id, existente);
    }

    @Override
    public Mesa cambiarEstado(Long id, EstadoMesa nuevoEstado) {
        Mesa mesa = buscarPorId(id);
        mesa.setEstado(nuevoEstado);
        log.info("Mesa id={} -> {}", id, nuevoEstado);
        return reemplazar(id, mesa);
    }

    @Override
    public void eliminar(Long id) {
        Mesa mesa = buscarPorId(id);
        if (mesa.estaOcupada()) {
            throw new EstadoInvalidoException("No se puede eliminar la mesa " + mesa.getNumero() + " porque esta ocupada");
        }
        eliminarPorId(id);
    }
}
