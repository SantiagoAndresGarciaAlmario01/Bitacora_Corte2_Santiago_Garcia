package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.dto.request.CuentaRequestDTO;
import com.restaurante.model.dto.response.CuentaResponseDTO;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "ABIERTA")
    @Mapping(target = "total", constant = "0.0")
    Cuenta toDomain(CuentaRequestDTO dto);

    CuentaResponseDTO toResponse(Cuenta cuenta);

    List<CuentaResponseDTO> toResponseList(List<Cuenta> cuentas);
}
