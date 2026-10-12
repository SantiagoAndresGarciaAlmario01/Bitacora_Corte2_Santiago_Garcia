package com.restaurante.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.model.domain.Reserva;
import com.restaurante.model.dto.request.ReservaRequestDTO;
import com.restaurante.model.dto.response.ReservaResponseDTO;

class ReservaMapperTest {

    private ReservaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ReservaMapperImpl();
    }

    @Test
    @DisplayName("toDomain mapea los campos del request y arranca sin cancelar")
    void toDomain_debeMapearCamposYForzarNoCancelada() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(1);
        ReservaRequestDTO dto = new ReservaRequestDTO();
        dto.setIdMesa(2L);
        dto.setNombreCliente("Juan Perez");
        dto.setFechaHora(fecha);
        dto.setNumeroPersonas(4);

        Reserva reserva = mapper.toDomain(dto);

        assertNull(reserva.getId());
        assertEquals("Juan Perez", reserva.getNombreCliente());
        assertFalse(reserva.isCancelada());
    }

    @Test
    @DisplayName("toResponse mapea todos los campos e incluye si esta vigente")
    void toResponse_debeMapearCamposYCalcularVigente() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2);
        Reserva reserva = Reserva.builder()
                .id(1L)
                .idMesa(3L)
                .nombreCliente("Ana Lopez")
                .fechaHora(fecha)
                .numeroPersonas(2)
                .cancelada(false)
                .build();

        ReservaResponseDTO response = mapper.toResponse(reserva);

        assertEquals(1L, response.getId());
        assertEquals("Ana Lopez", response.getNombreCliente());
        assertTrue(response.isVigente());
    }

    @Test
    @DisplayName("toResponseList mapea cada elemento de la lista de dominio")
    void toResponseList_debeMapearCadaElemento() {
        Reserva reserva1 = Reserva.builder().id(1L).idMesa(1L).nombreCliente("Cliente 1")
                .fechaHora(LocalDateTime.now().plusDays(1)).numeroPersonas(2).build();
        Reserva reserva2 = Reserva.builder().id(2L).idMesa(2L).nombreCliente("Cliente 2")
                .fechaHora(LocalDateTime.now().plusDays(1)).numeroPersonas(3).build();

        List<ReservaResponseDTO> resultado = mapper.toResponseList(List.of(reserva1, reserva2));

        assertEquals(2, resultado.size());
        assertEquals("Cliente 2", resultado.get(1).getNombreCliente());
    }
}
