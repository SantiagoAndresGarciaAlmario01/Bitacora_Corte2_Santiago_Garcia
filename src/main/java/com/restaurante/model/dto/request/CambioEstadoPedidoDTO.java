package com.restaurante.model.dto.request;

import com.restaurante.model.domain.EstadoPedido;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoPedidoDTO {

    @NotNull(message = "El estado es obligatorio")
    private EstadoPedido estado;
}
