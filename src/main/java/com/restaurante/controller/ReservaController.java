package com.restaurante.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.restaurante.mapper.ReservaMapper;
import com.restaurante.model.domain.Reserva;
import com.restaurante.model.dto.request.ReservaRequestDTO;
import com.restaurante.model.dto.response.ReservaResponseDTO;
import com.restaurante.service.IReservaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservas", description = "CRUD de las reservas de mesa")
public class ReservaController {

    private final IReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @GetMapping
    @Operation(summary = "Listar todas las reservas", description = "Se puede filtrar por mesa con ?idMesa=")
    @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida correctamente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerTodas(@RequestParam(required = false) Long idMesa) {
        log.info("GET /api/v1/reservas - idMesa={}", idMesa);
        List<Reserva> reservas = idMesa == null ? reservaService.obtenerTodos() : reservaService.obtenerPorMesa(idMesa);
        return ResponseEntity.ok(reservaMapper.toResponseList(reservas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por id")
    @ApiResponse(responseCode = "200", description = "Reserva encontrada")
    @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        Reserva reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(reservaMapper.toResponse(reserva));
    }

    @PostMapping
    @Operation(summary = "Crear una reserva nueva")
    @ApiResponse(responseCode = "201", description = "Reserva creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<ReservaResponseDTO> crear(@RequestBody @Valid ReservaRequestDTO dto) {
        log.info("POST /api/v1/reservas - idMesa={}, cliente={}", dto.getIdMesa(), dto.getNombreCliente());
        Reserva reserva = reservaMapper.toDomain(dto);
        Reserva creada = reservaService.crear(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaMapper.toResponse(creada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una reserva existente")
    @ApiResponse(responseCode = "200", description = "Reserva actualizada")
    @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<ReservaResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid ReservaRequestDTO dto) {
        Reserva nuevosDatos = reservaMapper.toDomain(dto);
        Reserva actualizada = reservaService.actualizar(id, nuevosDatos);
        return ResponseEntity.ok(reservaMapper.toResponse(actualizada));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una reserva")
    @ApiResponse(responseCode = "200", description = "Reserva cancelada")
    @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<ReservaResponseDTO> cancelar(@PathVariable Long id) {
        Reserva actualizada = reservaService.cancelar(id);
        return ResponseEntity.ok(reservaMapper.toResponse(actualizada));
    }

    @PatchMapping("/{id}/reprogramar")
    @Operation(summary = "Reprogramar una reserva a una nueva fecha y hora")
    @ApiResponse(responseCode = "200", description = "Reserva reprogramada")
    @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id")
    @PreAuthorize("hasAnyRole('GERENTE', 'MESERO')")
    public ResponseEntity<ReservaResponseDTO> reprogramar(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora) {
        Reserva actualizada = reservaService.reprogramar(id, fechaHora);
        return ResponseEntity.ok(reservaMapper.toResponse(actualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una reserva")
    @ApiResponse(responseCode = "204", description = "Reserva eliminada")
    @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
