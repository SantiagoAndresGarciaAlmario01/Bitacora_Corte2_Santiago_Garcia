package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;

public interface IMesaService {

    List<Mesa> obtenerTodos();

    Mesa obtenerPorId(Long id);

    Mesa crear(Mesa mesa);

    Mesa actualizar(Long id, Mesa nuevosDatos);

    Mesa cambiarEstado(Long id, EstadoMesa estado);

    void eliminar(Long id);
}
