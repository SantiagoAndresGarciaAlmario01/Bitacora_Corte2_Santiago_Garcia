package com.restaurante.mapper;

import org.mapstruct.Mapper;

import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.persistence.entity.RegistroVehiculoEntity;

@Mapper(componentModel = "spring")
public interface RegistroVehiculoEntityMapper {

    RegistroVehiculoEntity toEntity(RegistroVehiculo registro);

    RegistroVehiculo toDomain(RegistroVehiculoEntity entity);
}
