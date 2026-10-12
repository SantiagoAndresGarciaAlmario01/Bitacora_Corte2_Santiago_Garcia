package com.restaurante.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.model.dto.request.RegistroVehiculoRequestDTO;
import com.restaurante.model.dto.response.RegistroVehiculoResponseDTO;

class RegistroVehiculoMapperTest {

    private RegistroVehiculoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RegistroVehiculoMapperImpl();
    }

    @Test
    @DisplayName("toDomain mapea la placa y asigna la hora de entrada automaticamente")
    void toDomain_debeMapearPlacaYAsignarHoraEntrada() {
        RegistroVehiculoRequestDTO dto = new RegistroVehiculoRequestDTO();
        dto.setPlaca("ABC123");

        RegistroVehiculo registro = mapper.toDomain(dto);

        assertNull(registro.getId());
        assertEquals("ABC123", registro.getPlaca());
        assertNotNull(registro.getHoraEntrada());
        assertNull(registro.getHoraSalida());
    }

    @Test
    @DisplayName("toResponse mapea todos los campos e incluye si esta activo")
    void toResponse_debeMapearCamposYCalcularActivo() {
        RegistroVehiculo registro = RegistroVehiculo.builder()
                .id(1L)
                .placa("XYZ789")
                .horaEntrada(LocalDateTime.now())
                .build();

        RegistroVehiculoResponseDTO response = mapper.toResponse(registro);

        assertEquals(1L, response.getId());
        assertEquals("XYZ789", response.getPlaca());
        assertTrue(response.isActivo());
    }

    @Test
    @DisplayName("toResponseList mapea cada elemento y refleja los que ya tienen salida registrada")
    void toResponseList_debeMapearCadaElemento() {
        RegistroVehiculo activo = RegistroVehiculo.builder().id(1L).placa("AAA111")
                .horaEntrada(LocalDateTime.now()).build();
        RegistroVehiculo salido = RegistroVehiculo.builder().id(2L).placa("BBB222")
                .horaEntrada(LocalDateTime.now().minusHours(2))
                .horaSalida(LocalDateTime.now()).build();

        List<RegistroVehiculoResponseDTO> resultado = mapper.toResponseList(List.of(activo, salido));

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).isActivo());
        assertFalse(resultado.get(1).isActivo());
    }
}
