package com.restaurante.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.model.domain.Plato;
import com.restaurante.model.dto.request.PlatoRequestDTO;
import com.restaurante.model.dto.response.PlatoResponseDTO;

class PlatoMapperTest {

    private PlatoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PlatoMapperImpl();
    }

    @Test
    @DisplayName("toDomain mapea los campos del request y arranca disponible=true con id nulo")
    void toDomain_debeMapearCamposYForzarDisponibleTrue() {
        PlatoRequestDTO dto = new PlatoRequestDTO();
        dto.setNombre("Roll California");
        dto.setPrecio(25000.0);
        dto.setCategoria("Roll");
        dto.setDescripcion("Roll de cangrejo, aguacate y pepino");

        Plato plato = mapper.toDomain(dto);

        assertNull(plato.getId());
        assertEquals("Roll California", plato.getNombre());
        assertEquals(25000.0, plato.getPrecio());
        assertEquals("Roll", plato.getCategoria());
        assertEquals("Roll de cangrejo, aguacate y pepino", plato.getDescripcion());
        assertTrue(plato.getDisponible());
    }

    @Test
    @DisplayName("toResponse mapea todos los campos del dominio, incluido el id")
    void toResponse_debeMapearTodosLosCampos() {
        Plato plato = Plato.builder()
                .id(1L)
                .nombre("Nigiri de salmon")
                .precio(18000.0)
                .categoria("Nigiri")
                .descripcion("Salmon fresco")
                .disponible(false)
                .build();

        PlatoResponseDTO response = mapper.toResponse(plato);

        assertEquals(1L, response.getId());
        assertEquals("Nigiri de salmon", response.getNombre());
        assertEquals(18000.0, response.getPrecio());
        assertEquals("Nigiri", response.getCategoria());
        assertEquals("Salmon fresco", response.getDescripcion());
        assertEquals(false, response.getDisponible());
    }

    @Test
    @DisplayName("toResponseList mapea cada elemento de la lista de dominio")
    void toResponseList_debeMapearCadaElemento() {
        Plato plato1 = Plato.builder().id(1L).nombre("Roll California").precio(25000.0)
                .categoria("Roll").disponible(true).build();
        Plato plato2 = Plato.builder().id(2L).nombre("Sashimi Mixto").precio(30000.0)
                .categoria("Sashimi").disponible(true).build();

        List<PlatoResponseDTO> resultado = mapper.toResponseList(List.of(plato1, plato2));

        assertEquals(2, resultado.size());
        assertEquals("Roll California", resultado.get(0).getNombre());
        assertEquals("Sashimi Mixto", resultado.get(1).getNombre());
    }
}
