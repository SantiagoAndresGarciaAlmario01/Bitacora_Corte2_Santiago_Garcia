package com.restaurante.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.mapper.PlatoMapper;
import com.restaurante.model.domain.Plato;
import com.restaurante.model.dto.response.PlatoResponseDTO;
import com.restaurante.service.IPlatoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Menu", description = "Vista publica del menu: solo platos disponibles")
public class MenuController {

    private final IPlatoService platoService;
    private final PlatoMapper platoMapper;

    @GetMapping
    @Operation(summary = "Ver el menu", description = "Lista solo los platos que estan disponibles ahora mismo.")
    @ApiResponse(responseCode = "200", description = "Menu obtenido correctamente")
    public ResponseEntity<List<PlatoResponseDTO>> verMenu() {
        log.info("GET /api/v1/menu");
        List<Plato> disponibles = platoService.obtenerDisponibles();
        return ResponseEntity.ok(platoMapper.toResponseList(disponibles));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Ver el menu por categoria", description = "Ej: Roll, Nigiri, Sashimi, Temaki, Entrada, Bebida.")
    @ApiResponse(responseCode = "200", description = "Platos de esa categoria obtenidos correctamente")
    public ResponseEntity<List<PlatoResponseDTO>> verPorCategoria(@PathVariable String categoria) {
        List<Plato> platos = platoService.obtenerPorCategoria(categoria);
        return ResponseEntity.ok(platoMapper.toResponseList(platos));
    }
}
