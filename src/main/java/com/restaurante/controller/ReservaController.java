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

import com.restaurante.mapper.ReservaMapper;
import com.restaurante.model.domain.Reserva;
import com.restaurante.model.dto.request.ReprogramarReservaDTO;
import com.restaurante.model.dto.request.ReservaRequestDTO;
import com.restaurante.model.dto.response.ReservaResponseDTO;
import com.restaurante.service.IReservaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservas", description = "Reservas de mesas")
public class ReservaController {

    private final IReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @GetMapping
    @Operation(summary = "Listar reservas", description = "Con proximas=true solo trae las vigentes, ordenadas por fecha.")
    @ApiResponse(responseCode = "200", description = "Lista de reservas")
    public ResponseEntity<List<ReservaResponseDTO>> listar(@RequestParam(defaultValue = "false") boolean proximas) {
        List<Reserva> reservas = proximas ? reservaService.obtenerProximas() : reservaService.obtenerTodas();
        return ResponseEntity.ok(reservaMapper.toResponseList(reservas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id")
    })
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.obtenerPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Crear una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "No existe la mesa"),
            @ApiResponse(responseCode = "409", description = "La mesa ya esta reservada a esa hora"),
            @ApiResponse(responseCode = "422", description = "La mesa no alcanza para esas personas")
    })
    public ResponseEntity<ReservaResponseDTO> crear(@RequestBody @Valid ReservaRequestDTO dto) {
        log.info("POST /api/v1/reservas mesa={} fecha={}", dto.getIdMesa(), dto.getFechaHora());
        Reserva creada = reservaService.crear(reservaMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaMapper.toResponse(creada));
    }

    @PatchMapping("/{id}/fecha")
    @Operation(summary = "Reprogramar una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva reprogramada"),
            @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id"),
            @ApiResponse(responseCode = "409", description = "Reserva cancelada o cruce de horario")
    })
    public ResponseEntity<ReservaResponseDTO> reprogramar(@PathVariable Long id,
                                                          @RequestBody @Valid ReprogramarReservaDTO dto) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.reprogramar(id, dto.getFechaHora())));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada"),
            @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id"),
            @ApiResponse(responseCode = "409", description = "La reserva ya estaba cancelada")
    })
    public ResponseEntity<ReservaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.cancelar(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reserva eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe una reserva con ese id")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
