package com.restaurante.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRETO = "S2F6ZU5vcmlTdXNoaUNyYWZ0RE9TVzIwMjYtMi1TYW50aWFnb0dhcmNpYQ==";
    private static final String OTRO_SECRETO = "T3Ryb1NlY3JldG9EaWZlcmVudGVQYXJhUHJ1ZWJhc0RPU1cyMDI2";

    private final JwtService jwtService = new JwtService(SECRETO, 60_000L);

    @Test
    @DisplayName("el token generado guarda el correo y el rol")
    void tokenGuardaCorreoYRol() {
        String token = jwtService.generarToken("gerente@kazenori.co", "GERENTE");

        assertThat(jwtService.extraerCorreo(token)).isEqualTo("gerente@kazenori.co");
        assertThat(jwtService.extraerRol(token)).isEqualTo("GERENTE");
        assertThat(jwtService.esValidoPara(token, "gerente@kazenori.co")).isTrue();
    }

    @Test
    @DisplayName("un token no es valido para otro usuario")
    void tokenDeOtroUsuario() {
        String token = jwtService.generarToken("mesero@kazenori.co", "MESERO");

        assertThat(jwtService.esValidoPara(token, "gerente@kazenori.co")).isFalse();
    }

    @Test
    @DisplayName("un token vencido no es valido")
    void tokenVencido() {
        JwtService vencido = new JwtService(SECRETO, -1_000L);
        String token = vencido.generarToken("mesero@kazenori.co", "MESERO");

        assertThat(jwtService.esValidoPara(token, "mesero@kazenori.co")).isFalse();
    }

    @Test
    @DisplayName("un token firmado con otra clave es rechazado")
    void tokenConOtraFirma() {
        String tokenAjeno = new JwtService(OTRO_SECRETO, 60_000L).generarToken("gerente@kazenori.co", "GERENTE");

        assertThat(jwtService.esValidoPara(tokenAjeno, "gerente@kazenori.co")).isFalse();
        assertThatThrownBy(() -> jwtService.extraerCorreo(tokenAjeno)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("un texto que no es JWT es rechazado")
    void textoBasura() {
        assertThat(jwtService.esValidoPara("esto.no.es-un-token", "gerente@kazenori.co")).isFalse();
    }
}
