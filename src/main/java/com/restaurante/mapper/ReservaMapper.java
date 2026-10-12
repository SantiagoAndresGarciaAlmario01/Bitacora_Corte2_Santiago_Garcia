package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.Reserva;
import com.restaurante.model.dto.request.ReservaRequestDTO;
import com.restaurante.model.dto.response.ReservaResponseDTO;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cancelada", constant = "false")
    Reserva toDomain(ReservaRequestDTO dto);

    @Mapping(target = "vigente", expression = "java(reserva.estaVigente())")
    ReservaResponseDTO toResponse(Reserva reserva);

    List<ReservaResponseDTO> toResponseList(List<Reserva> reservas);
}
