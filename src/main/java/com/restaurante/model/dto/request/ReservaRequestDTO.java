package com.restaurante.model.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequestDTO {

    @NotNull(message = "El id de la mesa es obligatorio")
    @Positive(message = "El id de la mesa debe ser valido")
    private Long idMesa;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha debe ser en el futuro")
    private LocalDateTime fechaHora;

    @NotNull(message = "El numero de personas es obligatorio")
    @Positive(message = "El numero de personas debe ser mayor a 0")
    private Integer numeroPersonas;
}
