package com.restaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.model.dto.request.AbrirCuentaDTO;
import com.restaurante.model.dto.response.ConsumoCuentaResponseDTO;
import com.restaurante.model.dto.response.CuentaResponseDTO;
import com.restaurante.service.ICuentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cuentas", description = "Apertura, consumo y cierre de la cuenta de una mesa")
public class CuentaController {

    private final ICuentaService cuentaService;
    private final CuentaMapper cuentaMapper;

    @GetMapping
    @Operation(summary = "Listar cuentas", description = "Filtro opcional por estado: ABIERTA o CERRADA.")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas")
    public ResponseEntity<List<CuentaResponseDTO>> listar(@RequestParam(required = false) EstadoCuenta estado) {
        List<Cuenta> cuentas = estado == null ? cuentaService.obtenerTodas() : cuentaService.obtenerPorEstado(estado);
        return ResponseEntity.ok(cuentaMapper.toResponseList(cuentas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una cuenta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese id")
    })
    public ResponseEntity<CuentaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaMapper.toResponse(cuentaService.obtenerPorId(id)));
    }

    @GetMapping("/{id}/consumo")
    @Operation(summary = "Consultar lo consumido hasta ahora", description = "Suma los pedidos no cancelados de la cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consumo calculado"),
            @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese id")
    })
    public ResponseEntity<ConsumoCuentaResponseDTO> consumo(@PathVariable Long id) {
        Cuenta cuenta = cuentaService.obtenerPorId(id);
        ConsumoCuentaResponseDTO dto = ConsumoCuentaResponseDTO.builder()
                .idCuenta(id)
                .idMesa(cuenta.getIdMesa())
                .consumoActual(cuentaService.calcularConsumoActual(id))
                .build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Abrir la cuenta de una mesa", description = "La mesa pasa a OCUPADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta abierta"),
            @ApiResponse(responseCode = "404", description = "No existe la mesa"),
            @ApiResponse(responseCode = "409", description = "La mesa ya tiene una cuenta abierta")
    })
    public ResponseEntity<CuentaResponseDTO> abrir(@RequestBody @Valid AbrirCuentaDTO dto) {
        log.info("POST /api/v1/cuentas mesa={}", dto.getIdMesa());
        Cuenta cuenta = cuentaService.abrir(dto.getIdMesa());
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaMapper.toResponse(cuenta));
    }

    @PatchMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar una cuenta",
            description = "Cobra los pedidos ENTREGADOS y libera la mesa. Falla si quedan pedidos en curso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta cerrada con su total"),
            @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese id"),
            @ApiResponse(responseCode = "409", description = "La cuenta ya estaba cerrada"),
            @ApiResponse(responseCode = "422", description = "Hay pedidos pendientes")
    })
    public ResponseEntity<CuentaResponseDTO> cerrar(@PathVariable Long id) {
        log.info("PATCH /api/v1/cuentas/{}/cerrar", id);
        return ResponseEntity.ok(cuentaMapper.toResponse(cuentaService.cerrar(id)));
    }
}
