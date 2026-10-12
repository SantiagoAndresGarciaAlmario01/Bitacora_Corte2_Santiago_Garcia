package com.restaurante.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroVehiculoResponseDTO {

    private Long id;
    private String placa;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private boolean activo;
}
