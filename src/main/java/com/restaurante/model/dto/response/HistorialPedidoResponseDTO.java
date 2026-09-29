package com.restaurante.model.dto.response;

import java.time.LocalDateTime;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoEventoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialPedidoResponseDTO {

    private String id;
    private Long idPedido;
    private TipoEventoPedido tipo;
    private EstadoPedido estadoAnterior;
    private EstadoPedido estadoNuevo;
    private String detalle;
    private String usuario;
    private LocalDateTime fecha;
}
