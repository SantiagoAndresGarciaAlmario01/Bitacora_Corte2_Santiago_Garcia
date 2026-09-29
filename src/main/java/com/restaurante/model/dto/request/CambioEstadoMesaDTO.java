package com.restaurante.model.dto.request;

import com.restaurante.model.domain.EstadoMesa;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoMesaDTO {

    @NotNull(message = "El estado es obligatorio (LIBRE, OCUPADA o RESERVADA)")
    private EstadoMesa estado;
}
