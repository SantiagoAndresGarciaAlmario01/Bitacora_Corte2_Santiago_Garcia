package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.Cuenta;

public interface ICuentaService {

    List<Cuenta> obtenerTodos();

    List<Cuenta> obtenerPorMesa(Long idMesa);

    Cuenta obtenerPorId(Long id);

    Cuenta crear(Cuenta cuenta);

    Cuenta actualizarTotal(Long id, Double total);

    Cuenta cerrar(Long id);

    void eliminar(Long id);
}
