package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    private Long id;
    private Long idMesa;
    private EstadoCuenta estado;
    private Double total;

    public boolean estaAbierta() {
        return estado == EstadoCuenta.ABIERTA;
    }

    public void cerrar() {
        this.estado = EstadoCuenta.CERRADA;
    }
}
