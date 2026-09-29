package com.restaurante.service.impl;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.exception.ConflictoException;
import com.restaurante.model.domain.Rol;
import com.restaurante.model.dto.request.LoginRequestDTO;
import com.restaurante.model.dto.request.RegistroUsuarioDTO;
import com.restaurante.model.dto.response.TokenResponseDTO;
import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.persistence.repository.UsuarioRepository;
import com.restaurante.security.JwtService;
import com.restaurante.service.IAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDTO iniciarSesion(LoginRequestDTO credenciales) {
        // Si la contrasena no coincide, authenticate() lanza BadCredentialsException (-> 401)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credenciales.getCorreo(), credenciales.getContrasena()));

        UsuarioEntity usuario = usuarioRepository.findByCorreoIgnoreCase(credenciales.getCorreo())
                .orElseThrow();
        log.info("Inicio de sesion: {} ({})", usuario.getCorreo(), usuario.getRol());
        return construirToken(usuario);
    }

    @Override
    @Transactional
    public TokenResponseDTO registrarCliente(RegistroUsuarioDTO datos) {
        String correo = datos.getCorreo().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("Ya existe un usuario registrado con el correo " + correo);
        }
        UsuarioEntity nuevo = UsuarioEntity.builder()
                .nombre(datos.getNombre().trim())
                .correo(correo)
                .contrasenaHash(passwordEncoder.encode(datos.getContrasena()))
                .rol(Rol.CLIENTE)
                .build();
        UsuarioEntity guardado = usuarioRepository.save(nuevo);
        log.info("Nuevo cliente registrado: {}", correo);
        return construirToken(guardado);
    }

    private TokenResponseDTO construirToken(UsuarioEntity usuario) {
        return TokenResponseDTO.builder()
                .token(jwtService.generarToken(usuario.getCorreo(), usuario.getRol().name()))
                .tipo("Bearer")
                .expiraEnSegundos(jwtService.getExpiracionSegundos())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .build();
    }
}
