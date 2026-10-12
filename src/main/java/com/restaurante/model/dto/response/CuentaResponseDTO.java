package com.restaurante.model.dto.response;

import com.restaurante.model.domain.EstadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaResponseDTO {

    private Long id;
    private Long idMesa;
    private EstadoCuenta estado;
    private Double total;
}
