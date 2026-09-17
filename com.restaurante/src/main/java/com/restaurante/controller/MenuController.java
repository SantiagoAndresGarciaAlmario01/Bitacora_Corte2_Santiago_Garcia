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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final IPlatoService platoService;
    private final PlatoMapper platoMapper;

    @GetMapping
    public ResponseEntity<List<PlatoResponseDTO>> verMenu() {
        log.info("GET /api/menu");
        List<Plato> disponibles = platoService.obtenerDisponibles();
        return ResponseEntity.ok(platoMapper.toResponseList(disponibles));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<PlatoResponseDTO>> verPorCategoria(@PathVariable String categoria) {
        List<Plato> platos = platoService.obtenerPorCategoria(categoria);
        return ResponseEntity.ok(platoMapper.toResponseList(platos));
    }
}
