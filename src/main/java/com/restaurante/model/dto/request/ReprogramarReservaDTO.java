package com.restaurante.model.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReprogramarReservaDTO {

    @NotNull(message = "La nueva fecha y hora son obligatorias")
    @Future(message = "La nueva fecha debe ser futura")
    private LocalDateTime fechaHora;
}
