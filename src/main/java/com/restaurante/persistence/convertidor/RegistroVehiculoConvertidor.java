package com.restaurante.persistence.convertidor;

import org.springframework.stereotype.Component;

import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.persistence.entity.RegistroVehiculoEntity;

@Component
public class RegistroVehiculoConvertidor implements ConvertidorEntidad<RegistroVehiculo, RegistroVehiculoEntity> {

    @Override
    public RegistroVehiculo aDominio(RegistroVehiculoEntity entidad) {
        return RegistroVehiculo.builder()
                .id(entidad.getId())
                .placa(entidad.getPlaca())
                .horaEntrada(entidad.getHoraEntrada())
                .horaSalida(entidad.getHoraSalida())
                .build();
    }

    @Override
    public RegistroVehiculoEntity aEntidad(RegistroVehiculo dominio) {
        return RegistroVehiculoEntity.builder()
                .id(dominio.getId())
                .placa(dominio.getPlaca())
                .horaEntrada(dominio.getHoraEntrada())
                .horaSalida(dominio.getHoraSalida())
                .build();
    }
}
