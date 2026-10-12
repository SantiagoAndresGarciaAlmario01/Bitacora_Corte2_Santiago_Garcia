package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurante.model.domain.Mesa;
import com.restaurante.model.dto.request.MesaRequestDTO;
import com.restaurante.model.dto.response.MesaResponseDTO;

@Mapper(componentModel = "spring")
public interface MesaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "LIBRE")
    Mesa toDomain(MesaRequestDTO dto);

    MesaResponseDTO toResponse(Mesa mesa);

    List<MesaResponseDTO> toResponseList(List<Mesa> mesas);
}
