package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.restaurante.exception.ConflictoException;
import com.restaurante.model.domain.Rol;
import com.restaurante.model.dto.request.LoginRequestDTO;
import com.restaurante.model.dto.request.RegistroUsuarioDTO;
import com.restaurante.model.dto.response.TokenResponseDTO;
import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.persistence.repository.UsuarioRepository;
import com.restaurante.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String SECRETO = "S2F6ZU5vcmlTdXNoaUNyYWZ0RE9TVzIwMjYtMi1TYW50aWFnb0dhcmNpYQ==";

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRETO, 3_600_000L);
        service = new AuthServiceImpl(authenticationManager, usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("iniciarSesion() con credenciales correctas devuelve un token con el rol del usuario")
    void loginCorrecto() {
        UsuarioEntity mesero = UsuarioEntity.builder().id(2L).nombre("Mesero").correo("mesero@kazenori.co")
                .contrasenaHash("hash").rol(Rol.MESERO).build();
        when(usuarioRepository.findByCorreoIgnoreCase("mesero@kazenori.co")).thenReturn(Optional.of(mesero));

        TokenResponseDTO token = service.iniciarSesion(new LoginRequestDTO("mesero@kazenori.co", "Mesero2026*"));

        assertThat(token.getTipo()).isEqualTo("Bearer");
        assertThat(token.getRol()).isEqualTo(Rol.MESERO);
        assertThat(token.getExpiraEnSegundos()).isEqualTo(3600L);
        assertThat(jwtService.extraerCorreo(token.getToken())).isEqualTo("mesero@kazenori.co");
    }

    @Test
    @DisplayName("iniciarSesion() con contrasena incorrecta propaga BadCredentialsException")
    void loginIncorrecto() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        LoginRequestDTO credenciales = new LoginRequestDTO("mesero@kazenori.co", "mala");

        assertThatThrownBy(() -> service.iniciarSesion(credenciales)).isInstanceOf(BadCredentialsException.class);
        verify(usuarioRepository, never()).findByCorreoIgnoreCase(anyString());
    }

    @Test
    @DisplayName("registrarCliente() crea siempre un CLIENTE con la contrasena cifrada")
    void registrarCliente() {
        when(usuarioRepository.existsByCorreoIgnoreCase("nuevo@correo.com")).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura1")).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TokenResponseDTO token = service.registrarCliente(
                new RegistroUsuarioDTO("Nuevo Cliente", "  Nuevo@Correo.com ", "ClaveSegura1"));

        assertThat(token.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(token.getCorreo()).isEqualTo("nuevo@correo.com");
        verify(passwordEncoder).encode("ClaveSegura1");
    }

    @Test
    @DisplayName("registrarCliente() con un correo ya usado lanza ConflictoException")
    void registrarCorreoRepetido() {
        when(usuarioRepository.existsByCorreoIgnoreCase("mesero@kazenori.co")).thenReturn(true);
        RegistroUsuarioDTO datos = new RegistroUsuarioDTO("Otro", "mesero@kazenori.co", "ClaveSegura1");

        assertThatThrownBy(() -> service.registrarCliente(datos)).isInstanceOf(ConflictoException.class);
        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
    }
}
