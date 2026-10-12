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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.mapper.ItemPedidoMapper;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.dto.request.ItemPedidoRequestDTO;
import com.restaurante.model.dto.response.ItemPedidoResponseDTO;
import com.restaurante.service.IItemPedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/items-pedido")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Items de pedido", description = "CRUD de los items dentro de un pedido")
public class ItemPedidoController {

    private final IItemPedidoService itemPedidoService;
    private final ItemPedidoMapper itemPedidoMapper;

    @GetMapping
    @Operation(summary = "Listar todos los items de pedido", description = "Se puede filtrar por pedido con ?idPedido=")
    @ApiResponse(responseCode = "200", description = "Lista de items obtenida correctamente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ItemPedidoResponseDTO>> obtenerTodos(@RequestParam(required = false) Long idPedido) {
        log.info("GET /api/v1/items-pedido - idPedido={}", idPedido);
        List<ItemPedido> items = idPedido == null
                ? itemPedidoService.obtenerTodos()
                : itemPedidoService.obtenerPorPedido(idPedido);
        return ResponseEntity.ok(itemPedidoMapper.toResponseList(items));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un item de pedido por id")
    @ApiResponse(responseCode = "200", description = "Item encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un item con ese id")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ItemPedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        ItemPedido item = itemPedidoService.obtenerPorId(id);
        return ResponseEntity.ok(itemPedidoMapper.toResponse(item));
    }

    @PostMapping
    @Operation(summary = "Agregar un item a un pedido", description = "El nombre y precio del plato se copian ('congelan') al momento de crear el item.")
    @ApiResponse(responseCode = "201", description = "Item creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "No existe el pedido o el plato indicado")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<ItemPedidoResponseDTO> crear(@RequestBody @Valid ItemPedidoRequestDTO dto) {
        log.info("POST /api/v1/items-pedido - idPedido={}, idPlato={}", dto.getIdPedido(), dto.getIdPlato());
        ItemPedido item = itemPedidoMapper.toDomain(dto);
        ItemPedido creado = itemPedidoService.crear(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemPedidoMapper.toResponse(creado));
    }

    @PatchMapping("/{id}/cantidad")
    @Operation(summary = "Cambiar la cantidad de un item")
    @ApiResponse(responseCode = "200", description = "Cantidad actualizada")
    @ApiResponse(responseCode = "404", description = "No existe un item con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<ItemPedidoResponseDTO> actualizarCantidad(@PathVariable Long id, @RequestParam Integer cantidad) {
        ItemPedido actualizado = itemPedidoService.actualizarCantidad(id, cantidad);
        return ResponseEntity.ok(itemPedidoMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un item de pedido")
    @ApiResponse(responseCode = "204", description = "Item eliminado")
    @ApiResponse(responseCode = "404", description = "No existe un item con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        itemPedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
