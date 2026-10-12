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

import com.restaurante.mapper.RegistroVehiculoMapper;
import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.model.dto.request.RegistroVehiculoRequestDTO;
import com.restaurante.model.dto.response.RegistroVehiculoResponseDTO;
import com.restaurante.service.IRegistroVehiculoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parqueadero", description = "Registro de entrada y salida de vehiculos")
public class RegistroVehiculoController {

    private final IRegistroVehiculoService registroService;
    private final RegistroVehiculoMapper registroMapper;

    @GetMapping
    @Operation(summary = "Listar todos los registros", description = "Se puede filtrar solo los activos con ?activos=true")
    @ApiResponse(responseCode = "200", description = "Lista de registros obtenida correctamente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RegistroVehiculoResponseDTO>> obtenerTodos(
            @RequestParam(required = false, defaultValue = "false") boolean activos) {
        log.info("GET /api/v1/vehiculos - activos={}", activos);
        List<RegistroVehiculo> registros = activos ? registroService.obtenerActivos() : registroService.obtenerTodos();
        return ResponseEntity.ok(registroMapper.toResponseList(registros));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un registro por id")
    @ApiResponse(responseCode = "200", description = "Registro encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistroVehiculoResponseDTO> obtenerPorId(@PathVariable Long id) {
        RegistroVehiculo registro = registroService.obtenerPorId(id);
        return ResponseEntity.ok(registroMapper.toResponse(registro));
    }

    @PostMapping
    @Operation(summary = "Registrar el ingreso de un vehiculo", description = "La hora de entrada se asigna automaticamente.")
    @ApiResponse(responseCode = "201", description = "Registro creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<RegistroVehiculoResponseDTO> crear(@RequestBody @Valid RegistroVehiculoRequestDTO dto) {
        log.info("POST /api/v1/vehiculos - placa={}", dto.getPlaca());
        RegistroVehiculo registro = registroMapper.toDomain(dto);
        RegistroVehiculo creado = registroService.crear(registro);
        return ResponseEntity.status(HttpStatus.CREATED).body(registroMapper.toResponse(creado));
    }

    @PatchMapping("/{id}/salida")
    @Operation(summary = "Registrar la salida de un vehiculo")
    @ApiResponse(responseCode = "200", description = "Salida registrada")
    @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<RegistroVehiculoResponseDTO> registrarSalida(@PathVariable Long id) {
        RegistroVehiculo actualizado = registroService.registrarSalida(id);
        return ResponseEntity.ok(registroMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de vehiculo")
    @ApiResponse(responseCode = "204", description = "Registro eliminado")
    @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        registroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
