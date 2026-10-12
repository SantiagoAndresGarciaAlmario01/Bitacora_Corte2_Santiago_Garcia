package com.restaurante.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoRequestDTO {

    @NotNull(message = "El id del pedido es obligatorio")
    private Long idPedido;

    @NotNull(message = "El id del plato es obligatorio")
    private Long idPlato;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
}
