package com.restaurante.mapper;

import org.mapstruct.Mapper;

import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.PlatoEntity;

@Mapper(componentModel = "spring")
public interface PlatoEntityMapper {

    PlatoEntity toEntity(Plato plato);

    Plato toDomain(PlatoEntity entity);
}
