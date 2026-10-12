package com.restaurante.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.model.dto.request.MesaRequestDTO;
import com.restaurante.model.dto.response.MesaResponseDTO;

class MesaMapperTest {

    private MesaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MesaMapperImpl();
    }

    @Test
    @DisplayName("toDomain mapea los campos del request y arranca en estado LIBRE con id nulo")
    void toDomain_debeMapearCamposYForzarEstadoLibre() {
        MesaRequestDTO dto = new MesaRequestDTO();
        dto.setNumero(5);
        dto.setCapacidad(4);

        Mesa mesa = mapper.toDomain(dto);

        assertNull(mesa.getId());
        assertEquals(5, mesa.getNumero());
        assertEquals(4, mesa.getCapacidad());
        assertEquals(EstadoMesa.LIBRE, mesa.getEstado());
    }

    @Test
    @DisplayName("toResponse mapea todos los campos del dominio, incluido el id")
    void toResponse_debeMapearTodosLosCampos() {
        Mesa mesa = Mesa.builder().id(1L).numero(3).capacidad(6).estado(EstadoMesa.OCUPADA).build();

        MesaResponseDTO response = mapper.toResponse(mesa);

        assertEquals(1L, response.getId());
        assertEquals(3, response.getNumero());
        assertEquals(6, response.getCapacidad());
        assertEquals(EstadoMesa.OCUPADA, response.getEstado());
    }

    @Test
    @DisplayName("toResponseList mapea cada elemento de la lista de dominio")
    void toResponseList_debeMapearCadaElemento() {
        Mesa mesa1 = Mesa.builder().id(1L).numero(1).capacidad(2).estado(EstadoMesa.LIBRE).build();
        Mesa mesa2 = Mesa.builder().id(2L).numero(2).capacidad(4).estado(EstadoMesa.LIBRE).build();

        List<MesaResponseDTO> resultado = mapper.toResponseList(List.of(mesa1, mesa2));

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getNumero());
        assertEquals(2, resultado.get(1).getNumero());
    }
}
