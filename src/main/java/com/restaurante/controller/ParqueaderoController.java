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

import com.restaurante.mapper.RegistroVehiculoMapper;
import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.model.dto.request.EntradaVehiculoDTO;
import com.restaurante.model.dto.response.ParqueaderoEstadoResponseDTO;
import com.restaurante.model.dto.response.RegistroVehiculoResponseDTO;
import com.restaurante.service.IRegistroVehiculoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/parqueadero")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parqueadero", description = "Entradas y salidas de vehiculos de clientes")
public class ParqueaderoController {

    private final IRegistroVehiculoService registroService;
    private final RegistroVehiculoMapper registroMapper;

    @GetMapping
    @Operation(summary = "Listar registros", description = "Con activos=true solo los vehiculos que siguen dentro.")
    @ApiResponse(responseCode = "200", description = "Lista de registros")
    public ResponseEntity<List<RegistroVehiculoResponseDTO>> listar(@RequestParam(defaultValue = "false") boolean activos) {
        List<RegistroVehiculo> registros = activos ? registroService.obtenerActivos() : registroService.obtenerTodos();
        return ResponseEntity.ok(registroMapper.toResponseList(registros));
    }

    @GetMapping("/estado")
    @Operation(summary = "Cupos disponibles del parqueadero")
    @ApiResponse(responseCode = "200", description = "Estado actual")
    public ResponseEntity<ParqueaderoEstadoResponseDTO> estado() {
        ParqueaderoEstadoResponseDTO dto = ParqueaderoEstadoResponseDTO.builder()
                .cuposDisponibles(registroService.cuposDisponibles())
                .vehiculosDentro(registroService.obtenerActivos().size())
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un registro por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    public ResponseEntity<RegistroVehiculoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(registroMapper.toResponse(registroService.obtenerPorId(id)));
    }

    @PostMapping("/entradas")
    @Operation(summary = "Registrar la entrada de un vehiculo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrada registrada"),
            @ApiResponse(responseCode = "400", description = "Placa invalida"),
            @ApiResponse(responseCode = "409", description = "El vehiculo ya esta dentro"),
            @ApiResponse(responseCode = "422", description = "Parqueadero lleno")
    })
    public ResponseEntity<RegistroVehiculoResponseDTO> registrarEntrada(@RequestBody @Valid EntradaVehiculoDTO dto) {
        log.info("POST /api/v1/parqueadero/entradas placa={}", dto.getPlaca());
        RegistroVehiculo registro = registroService.registrarEntrada(dto.getPlaca());
        return ResponseEntity.status(HttpStatus.CREATED).body(registroMapper.toResponse(registro));
    }

    @PatchMapping("/{id}/salida")
    @Operation(summary = "Registrar la salida de un vehiculo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salida registrada"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id"),
            @ApiResponse(responseCode = "409", description = "El vehiculo ya habia salido")
    })
    public ResponseEntity<RegistroVehiculoResponseDTO> registrarSalida(@PathVariable Long id) {
        return ResponseEntity.ok(registroMapper.toResponse(registroService.registrarSalida(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        registroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
