package com.restaurante.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaRequestDTO {

    @NotNull(message = "El id de la mesa es obligatorio")
    @Positive(message = "El id de la mesa debe ser valido")
    private Long idMesa;
}
