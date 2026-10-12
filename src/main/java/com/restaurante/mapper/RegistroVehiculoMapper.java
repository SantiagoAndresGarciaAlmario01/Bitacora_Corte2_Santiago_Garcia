package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.model.dto.request.RegistroVehiculoRequestDTO;
import com.restaurante.model.dto.response.RegistroVehiculoResponseDTO;

@Mapper(componentModel = "spring")
public interface RegistroVehiculoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "horaEntrada", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "horaSalida", ignore = true)
    RegistroVehiculo toDomain(RegistroVehiculoRequestDTO dto);

    @Mapping(target = "activo", expression = "java(registro.estaActivo())")
    RegistroVehiculoResponseDTO toResponse(RegistroVehiculo registro);

    List<RegistroVehiculoResponseDTO> toResponseList(List<RegistroVehiculo> registros);
}
