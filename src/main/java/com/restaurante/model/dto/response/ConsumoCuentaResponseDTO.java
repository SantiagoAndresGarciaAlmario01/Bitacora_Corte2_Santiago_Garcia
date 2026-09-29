package com.restaurante.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumoCuentaResponseDTO {

    private Long idCuenta;
    private Long idMesa;
    private Double consumoActual;
}
