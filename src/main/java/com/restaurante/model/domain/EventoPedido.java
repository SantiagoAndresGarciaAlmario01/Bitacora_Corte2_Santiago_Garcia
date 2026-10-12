package com.restaurante.model.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoPedido {

    private String id;
    private Long idPedido;
    private String tipo;
    private String descripcion;
    private LocalDateTime timestamp;
}
