package com.restaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.mapper.PedidoMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.dto.request.PedidoRequestDTO;
import com.restaurante.model.dto.response.PedidoResponseDTO;
import com.restaurante.service.IPedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "CRUD de los pedidos de cada mesa")
public class PedidoController {

    private final IPedidoService pedidoService;
    private final PedidoMapper pedidoMapper;

    @GetMapping
    @Operation(summary = "Listar todos los pedidos", description = "Se puede filtrar por mesa con ?idMesa=")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida correctamente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodos(@RequestParam(required = false) Long idMesa) {
        log.info("GET /api/v1/pedidos - idMesa={}", idMesa);
        List<Pedido> pedidos = idMesa == null ? pedidoService.obtenerTodos() : pedidoService.obtenerPorMesa(idMesa);
        return ResponseEntity.ok(pedidoMapper.toResponseList(pedidos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pedido por id")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un pedido con ese id")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        return ResponseEntity.ok(pedidoMapper.toResponse(pedido));
    }

    @PostMapping
    @Operation(summary = "Crear un pedido nuevo", description = "El pedido queda con estado RECIBIDO y sin items.")
    @ApiResponse(responseCode = "201", description = "Pedido creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<PedidoResponseDTO> crear(@RequestBody @Valid PedidoRequestDTO dto) {
        log.info("POST /api/v1/pedidos - idMesa={}", dto.getIdMesa());
        Pedido pedido = pedidoMapper.toDomain(dto);
        Pedido creado = pedidoService.crear(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoMapper.toResponse(creado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un pedido existente")
    @ApiResponse(responseCode = "200", description = "Pedido actualizado")
    @ApiResponse(responseCode = "404", description = "No existe un pedido con ese id")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<PedidoResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid PedidoRequestDTO dto) {
        Pedido nuevosDatos = pedidoMapper.toDomain(dto);
        Pedido actualizado = pedidoService.actualizar(id, nuevosDatos);
        return ResponseEntity.ok(pedidoMapper.toResponse(actualizado));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de un pedido", description = "RECIBIDO, EN_PREPARACION, LISTO, ENTREGADO o CANCELADO.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "No existe un pedido con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO', 'COCINERO')")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        Pedido actualizado = pedidoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(pedidoMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pedido")
    @ApiResponse(responseCode = "204", description = "Pedido eliminado")
    @ApiResponse(responseCode = "404", description = "No existe un pedido con ese id")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
