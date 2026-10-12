package com.restaurante.mapper;

import org.mapstruct.Mapper;

import com.restaurante.model.domain.Cuenta;
import com.restaurante.persistence.entity.CuentaEntity;

@Mapper(componentModel = "spring")
public interface CuentaEntityMapper {

    CuentaEntity toEntity(Cuenta cuenta);

    Cuenta toDomain(CuentaEntity entity);
}
