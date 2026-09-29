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
public class Reserva {

    private Long id;
    private Long idMesa;
    private String nombreCliente;
    private LocalDateTime fechaHora;
    private Integer numeroPersonas;
    private boolean cancelada;

    public boolean estaVigente() {
        return !cancelada && fechaHora != null && fechaHora.isAfter(LocalDateTime.now());
    }

    public void cancelar() {
        this.cancelada = true;
    }

    public void reprogramar(LocalDateTime nuevaFechaHora) {
        this.fechaHora = nuevaFechaHora;
    }

    /**
     * Dos reservas de la misma mesa chocan si sus horas estan a menos de
     * {@code margenHoras} horas de distancia (tiempo promedio de una comida).
     */
    public boolean chocaCon(LocalDateTime otraFechaHora, int margenHoras) {
        if (fechaHora == null || otraFechaHora == null) {
            return false;
        }
        long minutos = Math.abs(Duration.between(fechaHora, otraFechaHora).toMinutes());
        return minutos < margenHoras * 60L;
    }
}
