package com.restaurante.service;

import java.time.LocalDateTime;
import java.util.List;

import com.restaurante.model.domain.Reserva;

public interface IReservaService {

    List<Reserva> obtenerTodas();

    List<Reserva> obtenerProximas();

    Reserva obtenerPorId(Long id);

    Reserva crear(Reserva reserva);

    Reserva reprogramar(Long id, LocalDateTime nuevaFechaHora);

    Reserva cancelar(Long id);

    void eliminar(Long id);
}
