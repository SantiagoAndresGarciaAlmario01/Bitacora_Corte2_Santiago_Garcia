package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.Plato;

public interface IPlatoService {

    List<Plato> obtenerTodos();

    List<Plato> obtenerDisponibles();

    List<Plato> obtenerPorCategoria(String categoria);

    Plato obtenerPorId(Long id);

    Plato crear(Plato plato);

    Plato actualizar(Long id, Plato nuevosDatos);

    Plato cambiarDisponibilidad(Long id, boolean disponible);

    void eliminar(Long id);
}
