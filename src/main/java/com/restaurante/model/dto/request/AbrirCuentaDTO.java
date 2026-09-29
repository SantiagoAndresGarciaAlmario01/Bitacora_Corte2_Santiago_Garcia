package com.restaurante.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbrirCuentaDTO {

    @NotNull(message = "El id de la mesa es obligatorio")
    private Long idMesa;
}
