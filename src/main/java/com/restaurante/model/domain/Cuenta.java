package com.restaurante.model.domain;

import java.time.LocalDateTime;

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
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    public boolean estaAbierta() {
        return estado == EstadoCuenta.ABIERTA;
    }

    public void abrir() {
        this.estado = EstadoCuenta.ABIERTA;
        this.total = 0.0;
        this.fechaApertura = LocalDateTime.now();
        this.fechaCierre = null;
    }

    public void cerrar(double totalFinal) {
        this.estado = EstadoCuenta.CERRADA;
        this.total = totalFinal;
        this.fechaCierre = LocalDateTime.now();
    }
}
