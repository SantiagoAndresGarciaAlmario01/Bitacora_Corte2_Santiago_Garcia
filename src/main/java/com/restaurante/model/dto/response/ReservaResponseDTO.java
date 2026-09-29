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
public class ReservaResponseDTO {

    private Long id;
    private Long idMesa;
    private String nombreCliente;
    private LocalDateTime fechaHora;
    private Integer numeroPersonas;
    private boolean cancelada;
    private boolean vigente;
}
