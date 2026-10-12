package com.restaurante.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.mapper.EventoPedidoMapper;
import com.restaurante.model.domain.EventoPedido;
import com.restaurante.model.dto.response.EventoPedidoResponseDTO;
import com.restaurante.service.IEventoPedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pedidos/{idPedido}/eventos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Eventos de pedido", description = "Historial de cambios de estado de un pedido (MongoDB)")
public class EventoPedidoController {

    private final IEventoPedidoService eventoPedidoService;
    private final EventoPedidoMapper eventoPedidoMapper;

    @GetMapping
    @Operation(summary = "Ver el historial de eventos de un pedido")
    @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO', 'COCINERO')")
    public ResponseEntity<List<EventoPedidoResponseDTO>> obtenerPorPedido(@PathVariable Long idPedido) {
        log.info("GET /api/v1/pedidos/{}/eventos", idPedido);
        List<EventoPedido> eventos = eventoPedidoService.obtenerPorPedido(idPedido);
        return ResponseEntity.ok(eventos.stream().map(eventoPedidoMapper::toResponse).toList());
    }
}
