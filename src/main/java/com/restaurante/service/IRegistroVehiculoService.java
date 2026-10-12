package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.RegistroVehiculo;

public interface IRegistroVehiculoService {

    List<RegistroVehiculo> obtenerTodos();

    List<RegistroVehiculo> obtenerActivos();

    RegistroVehiculo obtenerPorId(Long id);

    RegistroVehiculo crear(RegistroVehiculo registro);

    RegistroVehiculo registrarSalida(Long id);

    void eliminar(Long id);
}
