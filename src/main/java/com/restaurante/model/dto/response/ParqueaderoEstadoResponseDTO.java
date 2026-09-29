package com.restaurante.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParqueaderoEstadoResponseDTO {

    private int cuposDisponibles;
    private int vehiculosDentro;
}
