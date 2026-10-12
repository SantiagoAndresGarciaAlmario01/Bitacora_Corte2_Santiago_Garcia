package com.restaurante.mapper;

import org.mapstruct.Mapper;

import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.entity.ReservaEntity;

@Mapper(componentModel = "spring")
public interface ReservaEntityMapper {

    ReservaEntity toEntity(Reserva reserva);

    Reserva toDomain(ReservaEntity entity);
}
