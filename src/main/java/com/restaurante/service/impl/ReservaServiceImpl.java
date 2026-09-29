package com.restaurante.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.exception.ReglaDeNegocioException;
import com.restaurante.model.domain.Mesa;
import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.convertidor.ConvertidorEntidad;
import com.restaurante.persistence.convertidor.ReservaConvertidor;
import com.restaurante.persistence.entity.ReservaEntity;
import com.restaurante.persistence.repository.ReservaRepository;
import com.restaurante.service.IMesaService;
import com.restaurante.service.IReservaService;

import lombok.extern.slf4j.Slf4j;

/**
 * Reglas de reservas:
 * <ul>
 *   <li>La fecha debe ser futura.</li>
 *   <li>La mesa debe tener capacidad para el numero de personas.</li>
 *   <li>Una mesa no puede tener dos reservas activas separadas por menos de
 *       {@code restaurante.reservas.margen-horas} horas.</li>
 *   <li>Una reserva cancelada no se puede cancelar ni reprogramar de nuevo.</li>
 * </ul>
 */
@Service
@Slf4j
@Transactional
public class ReservaServiceImpl extends AbstractCrudServiceImpl<Reserva, ReservaEntity> implements IReservaService {

    private final ReservaRepository reservaRepository;
    private final ReservaConvertidor reservaConvertidor;
    private final IMesaService mesaService;
    private final int margenHoras;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              ReservaConvertidor reservaConvertidor,
                              IMesaService mesaService,
                              @Value("${restaurante.reservas.margen-horas:2}") int margenHoras) {
        this.reservaRepository = reservaRepository;
        this.reservaConvertidor = reservaConvertidor;
        this.mesaService = mesaService;
        this.margenHoras = margenHoras;
    }

    @Override
    protected JpaRepository<ReservaEntity, Long> repositorio() {
        return reservaRepository;
    }

    @Override
    protected ConvertidorEntidad<Reserva, ReservaEntity> convertidor() {
        return reservaConvertidor;
    }

    @Override
    protected void asignarId(Reserva dominio, Long id) {
        dominio.setId(id);
    }

    @Override
    protected String nombreRecurso() {
        return "Reserva";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> obtenerTodas() {
        return listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> obtenerProximas() {
        return aDominio(reservaRepository.findByCanceladaFalseAndFechaHoraAfterOrderByFechaHoraAsc(LocalDateTime.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public Reserva obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    @Override
    public Reserva crear(Reserva reserva) {
        validarFechaFutura(reserva.getFechaHora());
        Mesa mesa = mesaService.obtenerPorId(reserva.getIdMesa());
        if (!mesa.alcanzaPara(reserva.getNumeroPersonas())) {
            throw new ReglaDeNegocioException("La mesa " + mesa.getNumero() + " es para " + mesa.getCapacidad()
                    + " personas y la reserva es para " + reserva.getNumeroPersonas());
        }
        validarSinCruces(reserva.getIdMesa(), reserva.getFechaHora(), null);
        reserva.setCancelada(false);
        Reserva creada = guardar(reserva);
        log.info("Reserva creada: id={} mesa={} fecha={}", creada.getId(), creada.getIdMesa(), creada.getFechaHora());
        return creada;
    }

    @Override
    public Reserva reprogramar(Long id, LocalDateTime nuevaFechaHora) {
        Reserva reserva = buscarPorId(id);
        if (reserva.isCancelada()) {
            throw new EstadoInvalidoException("No se puede reprogramar una reserva cancelada");
        }
        validarFechaFutura(nuevaFechaHora);
        validarSinCruces(reserva.getIdMesa(), nuevaFechaHora, id);
        reserva.reprogramar(nuevaFechaHora);
        return reemplazar(id, reserva);
    }

    @Override
    public Reserva cancelar(Long id) {
        Reserva reserva = buscarPorId(id);
        if (reserva.isCancelada()) {
            throw new EstadoInvalidoException("La reserva id=" + id + " ya estaba cancelada");
        }
        reserva.cancelar();
        return reemplazar(id, reserva);
    }

    @Override
    public void eliminar(Long id) {
        eliminarPorId(id);
    }

    private void validarFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora == null || !fechaHora.isAfter(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("La fecha de la reserva debe ser futura");
        }
    }

    private void validarSinCruces(Long idMesa, LocalDateTime fechaHora, Long idIgnorado) {
        boolean hayCruce = aDominio(reservaRepository.findByIdMesaAndCanceladaFalse(idMesa)).stream()
                .filter(r -> idIgnorado == null || !idIgnorado.equals(r.getId()))
                .anyMatch(r -> r.chocaCon(fechaHora, margenHoras));
        if (hayCruce) {
            throw new ConflictoException("La mesa ya tiene una reserva a menos de " + margenHoras
                    + " horas de " + fechaHora);
        }
    }
}
