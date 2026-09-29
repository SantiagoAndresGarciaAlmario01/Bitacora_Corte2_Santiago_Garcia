package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoEventoPedido;
import com.restaurante.persistence.document.HistorialPedidoDocument;
import com.restaurante.persistence.repository.HistorialPedidoRepository;

@ExtendWith(MockitoExtension.class)
class HistorialPedidoServiceImplTest {

    @Mock
    private HistorialPedidoRepository historialRepository;

    private HistorialPedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HistorialPedidoServiceImpl(historialRepository);
    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private HistorialPedidoDocument eventoGuardado() {
        ArgumentCaptor<HistorialPedidoDocument> captor = ArgumentCaptor.forClass(HistorialPedidoDocument.class);
        verify(historialRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("registrar() sin usuario autenticado guarda 'sistema' como autor")
    void registrarSinUsuario() {
        service.registrar(1L, TipoEventoPedido.PEDIDO_CREADO, null, EstadoPedido.RECIBIDO, "creado");

        HistorialPedidoDocument evento = eventoGuardado();
        assertThat(evento.getUsuario()).isEqualTo("sistema");
        assertThat(evento.getIdPedido()).isEqualTo(1L);
        assertThat(evento.getFecha()).isNotNull();
    }

    @Test
    @DisplayName("registrar() guarda el correo del usuario autenticado")
    void registrarConUsuario() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "cocina@kazenori.co", null, List.of(new SimpleGrantedAuthority("ROLE_COCINERO"))));

        service.registrar(1L, TipoEventoPedido.ESTADO_CAMBIADO, EstadoPedido.RECIBIDO,
                EstadoPedido.EN_PREPARACION, "RECIBIDO -> EN_PREPARACION");

        HistorialPedidoDocument evento = eventoGuardado();
        assertThat(evento.getUsuario()).isEqualTo("cocina@kazenori.co");
        assertThat(evento.getEstadoNuevo()).isEqualTo(EstadoPedido.EN_PREPARACION);
    }
}
