package com.restaurante.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.model.dto.request.LoginRequestDTO;
import com.restaurante.model.dto.response.TokenResponseDTO;
import com.restaurante.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticacion", description = "Login y generacion de token JWT")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT valido por 24 horas.")
    @ApiResponse(responseCode = "200", description = "Login correcto, token generado")
    @ApiResponse(responseCode = "401", description = "Email o contrasena incorrectos")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        log.info("POST /auth/login - email={}", dto.getEmail());

        authManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        UserDetails user = userDetailsService.loadUserByUsername(dto.getEmail());
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .toList();

        String token = jwtUtil.generarToken(dto.getEmail(), roles.stream().map(r -> "ROLE_" + r).toList());
        String rolPrincipal = roles.isEmpty() ? null : roles.get(0);

        return ResponseEntity.ok(TokenResponseDTO.builder()
                .token(token)
                .email(dto.getEmail())
                .rol(rolPrincipal)
                .build());
    }
}
