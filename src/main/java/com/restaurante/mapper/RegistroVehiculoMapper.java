package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.model.dto.response.RegistroVehiculoResponseDTO;

@Mapper(componentModel = "spring")
public interface RegistroVehiculoMapper {

    @Mapping(target = "activo", expression = "java(registro.estaActivo())")
    @Mapping(target = "minutosEstacionado", expression = "java(registro.minutosEstacionado())")
    RegistroVehiculoResponseDTO toResponse(RegistroVehiculo registro);

    List<RegistroVehiculoResponseDTO> toResponseList(List<RegistroVehiculo> registros);
}
