package com.restaurante.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 1, message = "La capacidad minima es 1 persona")
    @Max(value = 12, message = "La capacidad maxima es 12 personas")
    private Integer capacidad;
}
