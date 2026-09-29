package com.restaurante.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.restaurante.model.dto.response.HistorialPedidoResponseDTO;
import com.restaurante.persistence.document.HistorialPedidoDocument;

@Mapper(componentModel = "spring")
public interface HistorialPedidoMapper {

    HistorialPedidoResponseDTO toResponse(HistorialPedidoDocument evento);

    List<HistorialPedidoResponseDTO> toResponseList(List<HistorialPedidoDocument> eventos);
}
