package com.restaurante.service;

import com.restaurante.model.dto.request.LoginRequestDTO;
import com.restaurante.model.dto.request.RegistroUsuarioDTO;
import com.restaurante.model.dto.response.TokenResponseDTO;

public interface IAuthService {

    TokenResponseDTO iniciarSesion(LoginRequestDTO credenciales);

    /** Registro publico: siempre crea usuarios con rol CLIENTE. */
    TokenResponseDTO registrarCliente(RegistroUsuarioDTO datos);
}
