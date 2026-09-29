package com.restaurante.model.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequestDTO {

    @NotNull(message = "El id de la mesa es obligatorio")
    private Long idMesa;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "Maximo 100 caracteres")
    private String nombreCliente;

    @NotNull(message = "La fecha y hora son obligatorias (formato 2026-10-15T19:30:00)")
    @Future(message = "La reserva debe ser para una fecha futura")
    private LocalDateTime fechaHora;

    @NotNull(message = "El numero de personas es obligatorio")
    @Min(value = 1, message = "Minimo 1 persona")
    private Integer numeroPersonas;
}
