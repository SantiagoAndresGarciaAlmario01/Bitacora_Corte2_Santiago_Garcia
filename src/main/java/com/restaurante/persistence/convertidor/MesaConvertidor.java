package com.restaurante.persistence.convertidor;

import org.springframework.stereotype.Component;

import com.restaurante.model.domain.Mesa;
import com.restaurante.persistence.entity.MesaEntity;

@Component
public class MesaConvertidor implements ConvertidorEntidad<Mesa, MesaEntity> {

    @Override
    public Mesa aDominio(MesaEntity entidad) {
        return Mesa.builder()
                .id(entidad.getId())
                .numero(entidad.getNumero())
                .capacidad(entidad.getCapacidad())
                .estado(entidad.getEstado())
                .build();
    }

    @Override
    public MesaEntity aEntidad(Mesa dominio) {
        return MesaEntity.builder()
                .id(dominio.getId())
                .numero(dominio.getNumero())
                .capacidad(dominio.getCapacidad())
                .estado(dominio.getEstado())
                .build();
    }
}
