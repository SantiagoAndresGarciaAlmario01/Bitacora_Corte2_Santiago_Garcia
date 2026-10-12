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

import com.restaurante.mapper.MesaMapper;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.model.dto.request.MesaRequestDTO;
import com.restaurante.model.dto.response.MesaResponseDTO;
import com.restaurante.service.IMesaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/mesas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mesas", description = "CRUD de las mesas del restaurante")
public class MesaController {

    private final IMesaService mesaService;
    private final MesaMapper mesaMapper;

    @GetMapping
    @Operation(summary = "Listar todas las mesas")
    @ApiResponse(responseCode = "200", description = "Lista de mesas obtenida correctamente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MesaResponseDTO>> obtenerTodas() {
        log.info("GET /api/v1/mesas");
        List<Mesa> mesas = mesaService.obtenerTodos();
        return ResponseEntity.ok(mesaMapper.toResponseList(mesas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una mesa por id")
    @ApiResponse(responseCode = "200", description = "Mesa encontrada")
    @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MesaResponseDTO> obtenerPorId(@PathVariable Long id) {
        Mesa mesa = mesaService.obtenerPorId(id);
        return ResponseEntity.ok(mesaMapper.toResponse(mesa));
    }

    @PostMapping
    @Operation(summary = "Crear una mesa nueva", description = "La mesa se crea libre por defecto.")
    @ApiResponse(responseCode = "201", description = "Mesa creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<MesaResponseDTO> crear(@RequestBody @Valid MesaRequestDTO dto) {
        log.info("POST /api/v1/mesas - numero={}", dto.getNumero());
        Mesa mesa = mesaMapper.toDomain(dto);
        Mesa creada = mesaService.crear(mesa);
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaMapper.toResponse(creada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una mesa existente")
    @ApiResponse(responseCode = "200", description = "Mesa actualizada")
    @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<MesaResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid MesaRequestDTO dto) {
        Mesa nuevosDatos = mesaMapper.toDomain(dto);
        Mesa actualizada = mesaService.actualizar(id, nuevosDatos);
        return ResponseEntity.ok(mesaMapper.toResponse(actualizada));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de una mesa", description = "LIBRE, OCUPADA o RESERVADA.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<MesaResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoMesa estado) {
        Mesa actualizada = mesaService.cambiarEstado(id, estado);
        return ResponseEntity.ok(mesaMapper.toResponse(actualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una mesa")
    @ApiResponse(responseCode = "204", description = "Mesa eliminada")
    @ApiResponse(responseCode = "404", description = "No existe una mesa con ese id")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
