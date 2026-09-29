package com.restaurante.persistence.convertidor;

import org.springframework.stereotype.Component;

import com.restaurante.model.domain.Cuenta;
import com.restaurante.persistence.entity.CuentaEntity;

@Component
public class CuentaConvertidor implements ConvertidorEntidad<Cuenta, CuentaEntity> {

    @Override
    public Cuenta aDominio(CuentaEntity entidad) {
        return Cuenta.builder()
                .id(entidad.getId())
                .idMesa(entidad.getIdMesa())
                .estado(entidad.getEstado())
                .total(entidad.getTotal())
                .fechaApertura(entidad.getFechaApertura())
                .fechaCierre(entidad.getFechaCierre())
                .build();
    }

    @Override
    public CuentaEntity aEntidad(Cuenta dominio) {
        return CuentaEntity.builder()
                .id(dominio.getId())
                .idMesa(dominio.getIdMesa())
                .estado(dominio.getEstado())
                .total(dominio.getTotal())
                .fechaApertura(dominio.getFechaApertura())
                .fechaCierre(dominio.getFechaCierre())
                .build();
    }
}
