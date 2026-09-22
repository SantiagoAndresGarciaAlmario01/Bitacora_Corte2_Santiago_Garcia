package com.restaurante.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.restaurante.model.domain.Plato;
import com.restaurante.model.dto.request.PlatoRequestDTO;
import com.restaurante.model.dto.response.PlatoResponseDTO;

class PlatoMapperTest {

    private final PlatoMapper mapper = Mappers.getMapper(PlatoMapper.class);

    @Test
    @DisplayName("toDomain() ignora el id del DTO y marca el plato como disponible")
    void toDomainIgnoraIdYMarcaDisponibleComoTrue() {
        PlatoRequestDTO dto = new PlatoRequestDTO();
        dto.setNombre("Roll California");
        dto.setPrecio(25000.0);
        dto.setCategoria("Roll");
        dto.setDescripcion("Roll de cangrejo, aguacate y pepino");

        Plato plato = mapper.toDomain(dto);

        assertThat(plato.getId()).isNull();
        assertThat(plato.getDisponible()).isTrue();
        assertThat(plato.getNombre()).isEqualTo("Roll California");
        assertThat(plato.getPrecio()).isEqualTo(25000.0);
        assertThat(plato.getCategoria()).isEqualTo("Roll");
        assertThat(plato.getDescripcion()).isEqualTo("Roll de cangrejo, aguacate y pepino");
    }

    @Test
    @DisplayName("toResponse() copia todos los campos del dominio al DTO de respuesta")
    void toResponseCopiaTodosLosCampos() {
        Plato plato = Plato.builder()
                .id(1L)
                .nombre("Nigiri de salmon")
                .precio(18000.0)
                .categoria("Nigiri")
                .disponible(true)
                .descripcion("Nigiri fresco de salmon")
                .build();

        PlatoResponseDTO response = mapper.toResponse(plato);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Nigiri de salmon");
        assertThat(response.getPrecio()).isEqualTo(18000.0);
        assertThat(response.getCategoria()).isEqualTo("Nigiri");
        assertThat(response.getDisponible()).isTrue();
        assertThat(response.getDescripcion()).isEqualTo("Nigiri fresco de salmon");
    }

    @Test
    @DisplayName("toResponseList() mapea cada elemento manteniendo el orden")
    void toResponseListMapeaCadaElementoEnElMismoOrden() {
        Plato roll = Plato.builder().id(1L).nombre("Roll California").categoria("Roll")
                .precio(25000.0).disponible(true).build();
        Plato nigiri = Plato.builder().id(2L).nombre("Nigiri de salmon").categoria("Nigiri")
                .precio(18000.0).disponible(false).build();

        List<PlatoResponseDTO> respuestas = mapper.toResponseList(List.of(roll, nigiri));

        assertThat(respuestas).hasSize(2);
        assertThat(respuestas.get(0).getNombre()).isEqualTo("Roll California");
        assertThat(respuestas.get(1).getNombre()).isEqualTo("Nigiri de salmon");
        assertThat(respuestas.get(1).getDisponible()).isFalse();
    }
}
