package com.restaurante.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.model.dto.request.CuentaRequestDTO;
import com.restaurante.model.dto.response.CuentaResponseDTO;

class CuentaMapperTest {

    private CuentaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CuentaMapperImpl();
    }

    @Test
    @DisplayName("toDomain mapea idMesa y arranca ABIERTA con total en 0")
    void toDomain_debeMapearCamposYForzarEstadoAbierta() {
        CuentaRequestDTO dto = new CuentaRequestDTO();
        dto.setIdMesa(4L);

        Cuenta cuenta = mapper.toDomain(dto);

        assertNull(cuenta.getId());
        assertEquals(4L, cuenta.getIdMesa());
        assertEquals(EstadoCuenta.ABIERTA, cuenta.getEstado());
        assertEquals(0.0, cuenta.getTotal());
    }

    @Test
    @DisplayName("toResponse mapea todos los campos del dominio")
    void toResponse_debeMapearTodosLosCampos() {
        Cuenta cuenta = Cuenta.builder().id(1L).idMesa(2L).estado(EstadoCuenta.CERRADA).total(50000.0).build();

        CuentaResponseDTO response = mapper.toResponse(cuenta);

        assertEquals(1L, response.getId());
        assertEquals(EstadoCuenta.CERRADA, response.getEstado());
        assertEquals(50000.0, response.getTotal());
    }

    @Test
    @DisplayName("toResponseList mapea cada elemento de la lista de dominio")
    void toResponseList_debeMapearCadaElemento() {
        Cuenta cuenta1 = Cuenta.builder().id(1L).idMesa(1L).estado(EstadoCuenta.ABIERTA).total(0.0).build();
        Cuenta cuenta2 = Cuenta.builder().id(2L).idMesa(2L).estado(EstadoCuenta.ABIERTA).total(0.0).build();

        List<CuentaResponseDTO> resultado = mapper.toResponseList(List.of(cuenta1, cuenta2));

        assertEquals(2, resultado.size());
    }
}
