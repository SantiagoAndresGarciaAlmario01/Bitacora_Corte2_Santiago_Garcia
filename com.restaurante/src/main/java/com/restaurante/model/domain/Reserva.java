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
}
