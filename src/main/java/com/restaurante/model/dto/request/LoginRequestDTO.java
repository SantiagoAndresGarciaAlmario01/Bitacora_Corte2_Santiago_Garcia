package com.restaurante.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;
}
