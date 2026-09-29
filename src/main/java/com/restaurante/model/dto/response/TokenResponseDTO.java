package com.restaurante.model.dto.response;

import com.restaurante.model.domain.Rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDTO {

    private String token;
    private String tipo;
    private long expiraEnSegundos;
    private String correo;
    private Rol rol;
}
