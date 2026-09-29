package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.RegistroVehiculo;

public interface IRegistroVehiculoService {

    List<RegistroVehiculo> obtenerTodos();

    List<RegistroVehiculo> obtenerActivos();

    RegistroVehiculo obtenerPorId(Long id);

    RegistroVehiculo registrarEntrada(String placa);

    RegistroVehiculo registrarSalida(Long id);

    int cuposDisponibles();

    void eliminar(Long id);
}
