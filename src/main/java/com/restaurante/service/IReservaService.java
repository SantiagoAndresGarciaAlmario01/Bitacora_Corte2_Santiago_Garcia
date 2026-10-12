package com.restaurante.service;

import java.time.LocalDateTime;
import java.util.List;

import com.restaurante.model.domain.Reserva;

public interface IReservaService {

    List<Reserva> obtenerTodos();

    List<Reserva> obtenerPorMesa(Long idMesa);

    Reserva obtenerPorId(Long id);

    Reserva crear(Reserva reserva);

    Reserva actualizar(Long id, Reserva nuevosDatos);

    Reserva cancelar(Long id);

    Reserva reprogramar(Long id, LocalDateTime nuevaFechaHora);

    void eliminar(Long id);
}
