package com.restaurante.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.restaurante.model.domain.Mesa;
import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.model.domain.Reserva;
import com.restaurante.model.dto.request.MesaRequestDTO;
import com.restaurante.model.dto.request.ReservaRequestDTO;
import com.restaurante.model.dto.response.RegistroVehiculoResponseDTO;
import com.restaurante.model.dto.response.ReservaResponseDTO;

class ReservaYParqueaderoMapperTest {

    private final ReservaMapper reservaMapper = Mappers.getMapper(ReservaMapper.class);
    private final RegistroVehiculoMapper registroMapper = Mappers.getMapper(RegistroVehiculoMapper.class);
    private final MesaMapper mesaMapper = Mappers.getMapper(MesaMapper.class);

    @Test
    @DisplayName("ReservaMapper: el request no trae id y la respuesta calcula si esta vigente")
    void reservaMapper() {
        LocalDateTime manana = LocalDateTime.now().plusDays(1);
        Reserva reserva = reservaMapper.toDomain(new ReservaRequestDTO(3L, "Laura Gomez", manana, 2));

        assertThat(reserva.getId()).isNull();
        assertThat(reserva.isCancelada()).isFalse();

        reserva.setId(8L);
        ReservaResponseDTO dto = reservaMapper.toResponse(reserva);
        assertThat(dto.isVigente()).isTrue();
        assertThat(dto.getNombreCliente()).isEqualTo("Laura Gomez");
    }

    @Test
    @DisplayName("RegistroVehiculoMapper: marca como activo al vehiculo que no ha salido")
    void registroMapper() {
        RegistroVehiculo registro = RegistroVehiculo.builder().id(1L).placa("ABC123")
                .horaEntrada(LocalDateTime.now().minusMinutes(30)).build();

        RegistroVehiculoResponseDTO dto = registroMapper.toResponse(registro);

        assertThat(dto.isActivo()).isTrue();
        assertThat(dto.getMinutosEstacionado()).isBetween(29L, 31L);
    }

    @Test
    @DisplayName("MesaMapper: el request no define id ni estado")
    void mesaMapper() {
        Mesa mesa = mesaMapper.toDomain(new MesaRequestDTO(7, 4));

        assertThat(mesa.getId()).isNull();
        assertThat(mesa.getEstado()).isNull();
        assertThat(mesa.getNumero()).isEqualTo(7);
    }
}
