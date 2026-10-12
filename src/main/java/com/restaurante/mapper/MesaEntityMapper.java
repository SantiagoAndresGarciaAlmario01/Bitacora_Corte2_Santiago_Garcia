package com.restaurante.mapper;

import org.mapstruct.Mapper;

import com.restaurante.model.domain.Mesa;
import com.restaurante.persistence.entity.MesaEntity;

@Mapper(componentModel = "spring")
public interface MesaEntityMapper {

    MesaEntity toEntity(Mesa mesa);

    Mesa toDomain(MesaEntity entity);
}
