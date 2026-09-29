package com.restaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.restaurante.mapper.MesaMapper;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.model.dto.request.CambioEstadoMesaDTO;
import com.restaurante.model.dto.request.MesaRequestDTO;
import com.restaurante.model.dto.response.MesaResponseDTO;
import com.restaurante.service.IMesaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/mesas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mesas", description = "Administracion de las mesas del salon")
public class MesaController {

    private final IMesaService mesaService;
    private final MesaMapper mesaMapper;

    @GetMapping
    @Operation(summary = "Listar mesas", description = "Filtro opcional por estado: LIBRE, OCUPADA o RESERVADA.")
    @ApiResponse(responseCode = "200", description = "Lista de mesas")
    public ResponseEntity<List<MesaResponseDTO>> listar(@RequestParam(required = false) EstadoMesa estado) {
        log.info("GET /api/v1/mesas estado={}", estado);
        List<Mesa> mesas = estado == null ? mesaService.obtenerTodas() : mesaService.obtenerPorEstado(estado);
        return ResponseEntity.ok(mesaMapper.toResponseList(mesas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una mesa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesa encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id")
    })
    public ResponseEntity<MesaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mesaMapper.toResponse(mesaService.obtenerPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Crear una mesa", description = "La mesa se crea LIBRE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mesa creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una mesa con ese numero")
    })
    public ResponseEntity<MesaResponseDTO> crear(@RequestBody @Valid MesaRequestDTO dto) {
        log.info("POST /api/v1/mesas numero={}", dto.getNumero());
        Mesa creada = mesaService.crear(mesaMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaMapper.toResponse(creada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar numero y capacidad de una mesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesa actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id"),
            @ApiResponse(responseCode = "409", description = "Numero repetido o mesa ocupada")
    })
    public ResponseEntity<MesaResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid MesaRequestDTO dto) {
        Mesa actualizada = mesaService.actualizar(id, mesaMapper.toDomain(dto));
        return ResponseEntity.ok(mesaMapper.toResponse(actualizada));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de una mesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id")
    })
    public ResponseEntity<MesaResponseDTO> cambiarEstado(@PathVariable Long id,
                                                         @RequestBody @Valid CambioEstadoMesaDTO dto) {
        Mesa mesa = mesaService.cambiarEstado(id, dto.getEstado());
        return ResponseEntity.ok(mesaMapper.toResponse(mesa));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una mesa", description = "No se puede eliminar una mesa ocupada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mesa eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id"),
            @ApiResponse(responseCode = "409", description = "La mesa esta ocupada")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
