package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.restaurante.exception.ReglaDeNegocioException;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.convertidor.ReservaConvertidor;
import com.restaurante.persistence.entity.ReservaEntity;
import com.restaurante.persistence.repository.ReservaRepository;
import com.restaurante.service.IMesaService;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    private static final int MARGEN_HORAS = 2;

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private IMesaService mesaService;

    private ReservaServiceImpl service;
    private LocalDateTime manana;

    @BeforeEach
    void setUp() {
        service = new ReservaServiceImpl(reservaRepository, new ReservaConvertidor(), mesaService, MARGEN_HORAS);
        manana = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0);
    }

    private Reserva nuevaReserva(LocalDateTime fecha, int personas) {
        return Reserva.builder().idMesa(3L).nombreCliente("Laura Gomez").fechaHora(fecha).numeroPersonas(personas).build();
    }

    private ReservaEntity reservaGuardada(Long id, LocalDateTime fecha, boolean cancelada) {
        return ReservaEntity.builder().id(id).idMesa(3L).nombreCliente("Otro cliente")
                .fechaHora(fecha).numeroPersonas(2).cancelada(cancelada).build();
    }

    private void mesaDeCuatro() {
        when(mesaService.obtenerPorId(3L)).thenReturn(
                Mesa.builder().id(3L).numero(3).capacidad(4).estado(EstadoMesa.LIBRE).build());
    }

    private void guardarDevuelveLoMismo() {
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(inv -> {
            ReservaEntity e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
    }

    @Test
    @DisplayName("crear() rechaza fechas pasadas sin consultar la mesa")
    void crearFechaPasada() {
        Reserva pasada = nuevaReserva(LocalDateTime.now().minusHours(1), 2);

        assertThatThrownBy(() -> service.crear(pasada)).isInstanceOf(ReglaDeNegocioException.class);
        verifyNoInteractions(mesaService);
    }

    @Test
    @DisplayName("crear() rechaza reservas con mas personas que la capacidad de la mesa")
    void crearMesaPequena() {
        mesaDeCuatro();
        Reserva grande = nuevaReserva(manana, 6);

        assertThatThrownBy(() -> service.crear(grande))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("4");
    }

    @Test
    @DisplayName("crear() rechaza una reserva a menos de 2 horas de otra en la misma mesa")
    void crearConCruceDeHorario() {
        mesaDeCuatro();
        when(reservaRepository.findByIdMesaAndCanceladaFalse(3L))
                .thenReturn(List.of(reservaGuardada(9L, manana.plusMinutes(90), false)));
        Reserva reserva = nuevaReserva(manana, 2);

        assertThatThrownBy(() -> service.crear(reserva)).isInstanceOf(ConflictoException.class);
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("crear() acepta la reserva si la otra esta a 2 horas o mas")
    void crearSinCruce() {
        mesaDeCuatro();
        when(reservaRepository.findByIdMesaAndCanceladaFalse(3L))
                .thenReturn(List.of(reservaGuardada(9L, manana.plusHours(2), false)));
        guardarDevuelveLoMismo();

        Reserva creada = service.crear(nuevaReserva(manana, 4));

        assertThat(creada.getId()).isEqualTo(1L);
        assertThat(creada.isCancelada()).isFalse();
        assertThat(creada.estaVigente()).isTrue();
    }

    @Test
    @DisplayName("reprogramar() no choca consigo misma")
    void reprogramarIgnoraLaMismaReserva() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reservaGuardada(5L, manana, false)));
        when(reservaRepository.findByIdMesaAndCanceladaFalse(3L))
                .thenReturn(List.of(reservaGuardada(5L, manana, false)));
        when(reservaRepository.existsById(5L)).thenReturn(true);
        guardarDevuelveLoMismo();

        Reserva reprogramada = service.reprogramar(5L, manana.plusMinutes(30));

        assertThat(reprogramada.getFechaHora()).isEqualTo(manana.plusMinutes(30));
    }

    @Test
    @DisplayName("reprogramar() una reserva cancelada lanza EstadoInvalidoException")
    void reprogramarCancelada() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reservaGuardada(5L, manana, true)));
        LocalDateTime nuevaFecha = manana.plusDays(1);

        assertThatThrownBy(() -> service.reprogramar(5L, nuevaFecha)).isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    @DisplayName("cancelar() marca la reserva como cancelada")
    void cancelar() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reservaGuardada(5L, manana, false)));
        when(reservaRepository.existsById(5L)).thenReturn(true);
        guardarDevuelveLoMismo();

        Reserva cancelada = service.cancelar(5L);

        assertThat(cancelada.isCancelada()).isTrue();
        assertThat(cancelada.estaVigente()).isFalse();
    }

    @Test
    @DisplayName("cancelar() dos veces lanza EstadoInvalidoException")
    void cancelarDosVeces() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reservaGuardada(5L, manana, true)));

        assertThatThrownBy(() -> service.cancelar(5L)).isInstanceOf(EstadoInvalidoException.class);
    }
}
