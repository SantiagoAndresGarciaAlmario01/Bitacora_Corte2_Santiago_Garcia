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

import com.restaurante.mapper.CuentaMapper;
import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.dto.request.CuentaRequestDTO;
import com.restaurante.model.dto.response.CuentaResponseDTO;
import com.restaurante.service.ICuentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cuentas", description = "CRUD de las cuentas por mesa")
public class CuentaController {

    private final ICuentaService cuentaService;
    private final CuentaMapper cuentaMapper;

    @GetMapping
    @Operation(summary = "Listar todas las cuentas", description = "Se puede filtrar por mesa con ?idMesa=")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas obtenida correctamente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CuentaResponseDTO>> obtenerTodas(@RequestParam(required = false) Long idMesa) {
        log.info("GET /api/v1/cuentas - idMesa={}", idMesa);
        List<Cuenta> cuentas = idMesa == null ? cuentaService.obtenerTodos() : cuentaService.obtenerPorMesa(idMesa);
        return ResponseEntity.ok(cuentaMapper.toResponseList(cuentas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una cuenta por id")
    @ApiResponse(responseCode = "200", description = "Cuenta encontrada")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese id")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CuentaResponseDTO> obtenerPorId(@PathVariable Long id) {
        Cuenta cuenta = cuentaService.obtenerPorId(id);
        return ResponseEntity.ok(cuentaMapper.toResponse(cuenta));
    }

    @PostMapping
    @Operation(summary = "Abrir una cuenta nueva", description = "La cuenta se crea ABIERTA y en 0 por defecto.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<CuentaResponseDTO> crear(@RequestBody @Valid CuentaRequestDTO dto) {
        log.info("POST /api/v1/cuentas - idMesa={}", dto.getIdMesa());
        Cuenta cuenta = cuentaMapper.toDomain(dto);
        Cuenta creada = cuentaService.crear(cuenta);
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaMapper.toResponse(creada));
    }

    @PatchMapping("/{id}/total")
    @Operation(summary = "Actualizar el total acumulado de una cuenta")
    @ApiResponse(responseCode = "200", description = "Total actualizado")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<CuentaResponseDTO> actualizarTotal(@PathVariable Long id, @RequestParam Double total) {
        Cuenta actualizada = cuentaService.actualizarTotal(id, total);
        return ResponseEntity.ok(cuentaMapper.toResponse(actualizada));
    }

    @PatchMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar una cuenta")
    @ApiResponse(responseCode = "200", description = "Cuenta cerrada")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<CuentaResponseDTO> cerrar(@PathVariable Long id) {
        Cuenta actualizada = cuentaService.cerrar(id);
        return ResponseEntity.ok(cuentaMapper.toResponse(actualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una cuenta")
    @ApiResponse(responseCode = "204", description = "Cuenta eliminada")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese id")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
