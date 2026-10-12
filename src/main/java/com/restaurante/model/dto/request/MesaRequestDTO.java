package com.restaurante.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesaRequestDTO {

    @NotNull(message = "El numero de mesa es obligatorio")
    @Positive(message = "El numero de mesa debe ser mayor a 0")
    private Integer numero;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;
}
