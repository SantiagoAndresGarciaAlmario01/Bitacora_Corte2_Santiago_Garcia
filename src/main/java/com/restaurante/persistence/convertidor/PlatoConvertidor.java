package com.restaurante.persistence.convertidor;

import org.springframework.stereotype.Component;

import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.PlatoEntity;

@Component
public class PlatoConvertidor implements ConvertidorEntidad<Plato, PlatoEntity> {

    @Override
    public Plato aDominio(PlatoEntity entidad) {
        return Plato.builder()
                .id(entidad.getId())
                .nombre(entidad.getNombre())
                .precio(entidad.getPrecio())
                .categoria(entidad.getCategoria())
                .disponible(entidad.getDisponible())
                .descripcion(entidad.getDescripcion())
                .build();
    }

    @Override
    public PlatoEntity aEntidad(Plato dominio) {
        return PlatoEntity.builder()
                .id(dominio.getId())
                .nombre(dominio.getNombre())
                .precio(dominio.getPrecio())
                .categoria(dominio.getCategoria())
                .disponible(dominio.getDisponible())
                .descripcion(dominio.getDescripcion())
                .build();
    }
}
