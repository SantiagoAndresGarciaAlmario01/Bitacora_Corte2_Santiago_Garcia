package com.restaurante.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plato {

    private Long id;
    private String nombre;
    private Double precio;
    private String categoria;
    private Boolean disponible;
    private String descripcion;

    public boolean estaDisponible() {
        return Boolean.TRUE.equals(disponible);
    }

    public void activar() {
        this.disponible = true;
    }

    public void desactivar() {
        this.disponible = false;
    }
}
