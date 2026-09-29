package com.restaurante.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Maximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 60, message = "La contrasena debe tener entre 8 y 60 caracteres")
    private String contrasena;
}
