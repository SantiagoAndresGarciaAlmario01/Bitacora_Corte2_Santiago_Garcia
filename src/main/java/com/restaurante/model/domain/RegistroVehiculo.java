package com.restaurante.model.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroVehiculo {

    private Long id;
    private String placa;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;

    public boolean estaActivo() {
        return horaSalida == null;
    }

    public void registrarSalida() {
        this.horaSalida = LocalDateTime.now();
    }

    /**
     * Minutos que el vehiculo lleva (o duro) en el parqueadero.
     */
    public long minutosEstacionado() {
        if (horaEntrada == null) {
            return 0;
        }
        LocalDateTime fin = horaSalida != null ? horaSalida : LocalDateTime.now();
        return Duration.between(horaEntrada, fin).toMinutes();
    }
}
