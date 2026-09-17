package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {

    private Long id;
    private Integer numero;
    private Integer capacidad;
    private EstadoMesa estado;

    public boolean estaLibre() {
        return estado == EstadoMesa.LIBRE;
    }

    public void ocupar() {
        this.estado = EstadoMesa.OCUPADA;
    }

    public void liberar() {
        this.estado = EstadoMesa.LIBRE;
    }
}
