package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.persistence.convertidor.MesaConvertidor;
import com.restaurante.persistence.entity.MesaEntity;
import com.restaurante.persistence.repository.MesaRepository;

@ExtendWith(MockitoExtension.class)
class MesaServiceImplTest {

    @Mock
    private MesaRepository mesaRepository;

    private MesaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MesaServiceImpl(mesaRepository, new MesaConvertidor());
    }

    private MesaEntity mesa(Long id, int numero, int capacidad, EstadoMesa estado) {
        return MesaEntity.builder().id(id).numero(numero).capacidad(capacidad).estado(estado).build();
    }

    private void guardarDevuelveLoMismo() {
        when(mesaRepository.save(any(MesaEntity.class))).thenAnswer(inv -> {
            MesaEntity e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(10L);
            }
            return e;
        });
    }

    @Test
    @DisplayName("crear() deja la mesa LIBRE aunque llegue con otro estado")
    void crearQuedaLibre() {
        when(mesaRepository.existsByNumero(7)).thenReturn(false);
        guardarDevuelveLoMismo();

        Mesa creada = service.crear(Mesa.builder().numero(7).capacidad(4).estado(EstadoMesa.OCUPADA).build());

        assertThat(creada.getId()).isEqualTo(10L);
        assertThat(creada.getEstado()).isEqualTo(EstadoMesa.LIBRE);
    }

    @Test
    @DisplayName("crear() con un numero repetido lanza ConflictoException")
    void crearNumeroRepetido() {
        when(mesaRepository.existsByNumero(1)).thenReturn(true);
        Mesa repetida = Mesa.builder().numero(1).capacidad(2).build();

        assertThatThrownBy(() -> service.crear(repetida))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("1");
        verify(mesaRepository, never()).save(any(MesaEntity.class));
    }

    @Test
    @DisplayName("obtenerPorEstado() devuelve solo las mesas de ese estado")
    void obtenerPorEstado() {
        when(mesaRepository.findByEstado(EstadoMesa.LIBRE)).thenReturn(List.of(mesa(1L, 1, 2, EstadoMesa.LIBRE)));

        List<Mesa> libres = service.obtenerPorEstado(EstadoMesa.LIBRE);

        assertThat(libres).allMatch(Mesa::estaLibre);
    }

    @Test
    @DisplayName("actualizar() no permite cambiar la capacidad de una mesa ocupada")
    void actualizarCapacidadMesaOcupada() {
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesa(1L, 1, 2, EstadoMesa.OCUPADA)));
        when(mesaRepository.existsByNumeroAndIdNot(1, 1L)).thenReturn(false);
        Mesa nuevosDatos = Mesa.builder().numero(1).capacidad(6).build();

        assertThatThrownBy(() -> service.actualizar(1L, nuevosDatos))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    @DisplayName("actualizar() no permite usar el numero de otra mesa")
    void actualizarNumeroDeOtraMesa() {
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesa(1L, 1, 2, EstadoMesa.LIBRE)));
        when(mesaRepository.existsByNumeroAndIdNot(2, 1L)).thenReturn(true);
        Mesa nuevosDatos = Mesa.builder().numero(2).capacidad(2).build();

        assertThatThrownBy(() -> service.actualizar(1L, nuevosDatos))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    @DisplayName("actualizar() cambia numero y capacidad de una mesa libre")
    void actualizarMesaLibre() {
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesa(1L, 1, 2, EstadoMesa.LIBRE)));
        when(mesaRepository.existsByNumeroAndIdNot(9, 1L)).thenReturn(false);
        when(mesaRepository.existsById(1L)).thenReturn(true);
        guardarDevuelveLoMismo();

        Mesa actualizada = service.actualizar(1L, Mesa.builder().numero(9).capacidad(6).build());

        assertThat(actualizada.getNumero()).isEqualTo(9);
        assertThat(actualizada.getCapacidad()).isEqualTo(6);
        assertThat(actualizada.getEstado()).isEqualTo(EstadoMesa.LIBRE);
    }

    @Test
    @DisplayName("cambiarEstado() guarda el nuevo estado")
    void cambiarEstado() {
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesa(1L, 1, 2, EstadoMesa.LIBRE)));
        when(mesaRepository.existsById(1L)).thenReturn(true);
        guardarDevuelveLoMismo();

        Mesa mesa = service.cambiarEstado(1L, EstadoMesa.RESERVADA);

        assertThat(mesa.getEstado()).isEqualTo(EstadoMesa.RESERVADA);
    }

    @Test
    @DisplayName("eliminar() no borra una mesa ocupada")
    void eliminarMesaOcupada() {
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesa(1L, 1, 2, EstadoMesa.OCUPADA)));

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(EstadoInvalidoException.class);
        verify(mesaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("eliminar() borra una mesa libre")
    void eliminarMesaLibre() {
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesa(1L, 1, 2, EstadoMesa.LIBRE)));
        when(mesaRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(mesaRepository).deleteById(1L);
    }
}
