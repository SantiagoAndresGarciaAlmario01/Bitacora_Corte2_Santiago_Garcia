package com.restaurante.model.dto.response;

import com.restaurante.model.domain.EstadoMesa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesaResponseDTO {

    private Long id;
    private Integer numero;
    private Integer capacidad;
    private EstadoMesa estado;
}
