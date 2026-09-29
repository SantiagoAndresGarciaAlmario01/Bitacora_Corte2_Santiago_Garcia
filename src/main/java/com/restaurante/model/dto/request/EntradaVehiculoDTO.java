package com.restaurante.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntradaVehiculoDTO {

    /** Placa colombiana de carro (ABC123) o moto (ABC12D), con o sin guion. */
    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Za-z]{3}-?\\s?[0-9]{2}[0-9A-Za-z]$",
            message = "Placa invalida. Ejemplos validos: ABC123, ABC-123, ABC12D")
    private String placa;
}
