package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.dto.response.CuentaResponseDTO;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    CuentaResponseDTO toResponse(Cuenta cuenta);

    List<CuentaResponseDTO> toResponseList(List<Cuenta> cuentas);
}
