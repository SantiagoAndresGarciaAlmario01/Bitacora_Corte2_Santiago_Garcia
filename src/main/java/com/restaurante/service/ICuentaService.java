package com.restaurante.service;

import java.util.List;

import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoCuenta;

public interface ICuentaService {

    List<Cuenta> obtenerTodas();

    List<Cuenta> obtenerPorEstado(EstadoCuenta estado);

    Cuenta obtenerPorId(Long id);

    /** Abre la cuenta de una mesa y la marca como OCUPADA. */
    Cuenta abrir(Long idMesa);

    /** Devuelve la cuenta abierta de la mesa o lanza ReglaDeNegocioException. */
    Cuenta obtenerAbiertaDeMesa(Long idMesa);

    /** Suma lo consumido hasta ahora (pedidos no cancelados). */
    double calcularConsumoActual(Long idCuenta);

    /** Cierra la cuenta, fija el total cobrado y libera la mesa. */
    Cuenta cerrar(Long id);
}
