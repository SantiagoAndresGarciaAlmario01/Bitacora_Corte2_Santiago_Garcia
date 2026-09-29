package com.restaurante.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.model.dto.request.LoginRequestDTO;
import com.restaurante.model.dto.request.RegistroUsuarioDTO;
import com.restaurante.model.dto.response.TokenResponseDTO;
import com.restaurante.service.IAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticacion", description = "Login y registro para obtener el token JWT")
@SecurityRequirements
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Devuelve el token JWT que se envia como 'Authorization: Bearer <token>'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales correctas"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos"),
            @ApiResponse(responseCode = "401", description = "Correo o contrasena incorrectos")
    })
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        log.info("POST /api/v1/auth/login correo={}", dto.getCorreo());
        return ResponseEntity.ok(authService.iniciarSesion(dto));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrarse como cliente", description = "Crea un usuario con rol CLIENTE y devuelve su token.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente registrado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "409", description = "El correo ya esta registrado")
    })
    public ResponseEntity<TokenResponseDTO> registro(@RequestBody @Valid RegistroUsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarCliente(dto));
    }
}
