package com.restaurante.model.dto.response;

import java.time.LocalDateTime;

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
    /** Total cobrado (solo tiene sentido cuando la cuenta esta CERRADA). */
    private Double total;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
}
