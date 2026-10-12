package com.restaurante.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroVehiculoRequestDTO {

    @NotBlank(message = "La placa es obligatoria")
    private String placa;
}
