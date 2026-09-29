package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;

public interface IMesaService {

    List<Mesa> obtenerTodas();

    List<Mesa> obtenerPorEstado(EstadoMesa estado);

    Mesa obtenerPorId(Long id);

    Mesa crear(Mesa mesa);

    Mesa actualizar(Long id, Mesa nuevosDatos);

    Mesa cambiarEstado(Long id, EstadoMesa nuevoEstado);

    void eliminar(Long id);
}
