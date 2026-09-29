package com.restaurante.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoEventoPedido;
import com.restaurante.persistence.document.HistorialPedidoDocument;
import com.restaurante.persistence.repository.HistorialPedidoRepository;
import com.restaurante.service.IHistorialPedidoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistorialPedidoServiceImpl implements IHistorialPedidoService {

    static final String USUARIO_SISTEMA = "sistema";

    private final HistorialPedidoRepository historialRepository;

    @Override
    public void registrar(Long idPedido, TipoEventoPedido tipo, EstadoPedido estadoAnterior,
                          EstadoPedido estadoNuevo, String detalle) {
        HistorialPedidoDocument evento = HistorialPedidoDocument.builder()
                .idPedido(idPedido)
                .tipo(tipo)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .detalle(detalle)
                .usuario(usuarioActual())
                .fecha(LocalDateTime.now())
                .build();
        historialRepository.save(evento);
        log.debug("Historial pedido {}: {} ({})", idPedido, tipo, detalle);
    }

    @Override
    public List<HistorialPedidoDocument> obtenerPorPedido(Long idPedido) {
        return historialRepository.findByIdPedidoOrderByFechaAsc(idPedido);
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return USUARIO_SISTEMA;
        }
        return auth.getName();
    }
}
