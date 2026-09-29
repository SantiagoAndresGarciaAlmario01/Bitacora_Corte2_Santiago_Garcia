package com.restaurante.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.model.dto.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Hace que los errores 401 y 403 de Spring Security salgan con el mismo
 * formato {@link ErrorResponseDTO} que el resto de errores de la API.
 */
@Component
@RequiredArgsConstructor
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /** 401: no hay token o el token no es valido. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, request, HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesion: envia un token valido en el header Authorization (Bearer <token>)");
    }

    /** 403: el token es valido pero el rol no tiene permiso para esa operacion. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, request, HttpStatus.FORBIDDEN, "Tu rol no tiene permiso para esta operacion");
    }

    private void escribir(HttpServletResponse response, HttpServletRequest request,
                          HttpStatus status, String mensaje) throws IOException {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(mensaje)
                .path(request.getRequestURI())
                .build();
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
