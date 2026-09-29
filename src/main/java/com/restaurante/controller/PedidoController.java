package com.restaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.mapper.HistorialPedidoMapper;
import com.restaurante.mapper.PedidoMapper;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.dto.request.CambioEstadoPedidoDTO;
import com.restaurante.model.dto.request.ItemPedidoRequestDTO;
import com.restaurante.model.dto.request.PedidoRequestDTO;
import com.restaurante.model.dto.response.HistorialPedidoResponseDTO;
import com.restaurante.model.dto.response.PedidoResponseDTO;
import com.restaurante.service.IHistorialPedidoService;
import com.restaurante.service.IPedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "Pedidos de cada mesa, sus items y su paso por cocina")
public class PedidoController {

    private final IPedidoService pedidoService;
    private final IHistorialPedidoService historialService;
    private final PedidoMapper pedidoMapper;
    private final HistorialPedidoMapper historialMapper;

    @GetMapping
    @Operation(summary = "Listar pedidos",
            description = "Filtros opcionales: idMesa o estado (util para la pantalla de cocina: estado=RECIBIDO).")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listar(@RequestParam(required = false) Long idMesa,
                                                          @RequestParam(required = false) EstadoPedido estado) {
        List<Pedido> pedidos;
        if (idMesa != null) {
            pedidos = pedidoService.obtenerPorMesa(idMesa);
        } else if (estado != null) {
            pedidos = pedidoService.obtenerPorEstado(estado);
        } else {
            pedidos = pedidoService.obtenerTodos();
        }
        return ResponseEntity.ok(pedidoMapper.toResponseList(pedidos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pedido por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido con ese id")
    })
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoMapper.toResponse(pedidoService.obtenerPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Crear un pedido vacio para una mesa", description = "La mesa debe tener una cuenta abierta.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado en estado RECIBIDO"),
            @ApiResponse(responseCode = "422", description = "La mesa no tiene cuenta abierta")
    })
    public ResponseEntity<PedidoResponseDTO> crear(@RequestBody @Valid PedidoRequestDTO dto) {
        log.info("POST /api/v1/pedidos mesa={}", dto.getIdMesa());
        Pedido creado = pedidoService.crear(dto.getIdMesa());
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoMapper.toResponse(creado));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Agregar un plato al pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item agregado"),
            @ApiResponse(responseCode = "404", description = "No existe el pedido o el plato"),
            @ApiResponse(responseCode = "409", description = "El pedido ya no se puede modificar"),
            @ApiResponse(responseCode = "422", description = "El plato no esta disponible")
    })
    public ResponseEntity<PedidoResponseDTO> agregarItem(@PathVariable Long id,
                                                         @RequestBody @Valid ItemPedidoRequestDTO dto) {
        Pedido pedido = pedidoService.agregarItem(id, dto.getIdPlato(), dto.getCantidad());
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoMapper.toResponse(pedido));
    }

    @DeleteMapping("/{id}/items/{idItem}")
    @Operation(summary = "Retirar un item del pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item retirado"),
            @ApiResponse(responseCode = "404", description = "No existe el pedido o el item"),
            @ApiResponse(responseCode = "409", description = "El pedido ya no se puede modificar")
    })
    public ResponseEntity<PedidoResponseDTO> retirarItem(@PathVariable Long id, @PathVariable Long idItem) {
        return ResponseEntity.ok(pedidoMapper.toResponse(pedidoService.retirarItem(id, idItem)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de un pedido",
            description = "Flujo: RECIBIDO -> EN_PREPARACION -> LISTO -> ENTREGADO. Se puede CANCELAR antes de LISTO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido con ese id"),
            @ApiResponse(responseCode = "409", description = "Transicion de estado no permitida")
    })
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id,
                                                           @RequestBody @Valid CambioEstadoPedidoDTO dto) {
        log.info("PATCH /api/v1/pedidos/{}/estado -> {}", id, dto.getEstado());
        return ResponseEntity.ok(pedidoMapper.toResponse(pedidoService.cambiarEstado(id, dto.getEstado())));
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Historial de cambios del pedido (MongoDB)")
    @ApiResponse(responseCode = "200", description = "Eventos del pedido en orden cronologico")
    public ResponseEntity<List<HistorialPedidoResponseDTO>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(historialMapper.toResponseList(historialService.obtenerPorPedido(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pedido", description = "Solo pedidos en RECIBIDO o CANCELADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido con ese id"),
            @ApiResponse(responseCode = "409", description = "El pedido ya esta en cocina o entregado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
