package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.persistence.convertidor.RegistroVehiculoConvertidor;
import com.restaurante.persistence.entity.RegistroVehiculoEntity;
import com.restaurante.persistence.repository.RegistroVehiculoRepository;

@ExtendWith(MockitoExtension.class)
class RegistroVehiculoServiceImplTest {

    private static final int CUPOS = 2;

    @Mock
    private RegistroVehiculoRepository registroRepository;

    private RegistroVehiculoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RegistroVehiculoServiceImpl(registroRepository, new RegistroVehiculoConvertidor(), CUPOS);
    }

    private void guardarDevuelveLoMismo() {
        when(registroRepository.save(any(RegistroVehiculoEntity.class))).thenAnswer(inv -> {
            RegistroVehiculoEntity e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
    }

    @Test
    @DisplayName("registrarEntrada() guarda la placa en mayusculas y sin guion")
    void registrarEntradaNormalizaPlaca() {
        when(registroRepository.existsByPlacaAndHoraSalidaIsNull("ABC123")).thenReturn(false);
        when(registroRepository.countByHoraSalidaIsNull()).thenReturn(0L);
        guardarDevuelveLoMismo();

        RegistroVehiculo registro = service.registrarEntrada("abc-123");

        assertThat(registro.getPlaca()).isEqualTo("ABC123");
        assertThat(registro.getHoraEntrada()).isNotNull();
        assertThat(registro.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("registrarEntrada() rechaza un vehiculo que ya esta dentro")
    void registrarEntradaDuplicada() {
        when(registroRepository.existsByPlacaAndHoraSalidaIsNull("ABC123")).thenReturn(true);

        assertThatThrownBy(() -> service.registrarEntrada("ABC 123")).isInstanceOf(ConflictoException.class);
        verify(registroRepository, never()).save(any(RegistroVehiculoEntity.class));
    }

    @Test
    @DisplayName("registrarEntrada() rechaza la entrada si el parqueadero esta lleno")
    void registrarEntradaSinCupos() {
        when(registroRepository.existsByPlacaAndHoraSalidaIsNull("XYZ98K")).thenReturn(false);
        when(registroRepository.countByHoraSalidaIsNull()).thenReturn((long) CUPOS);

        assertThatThrownBy(() -> service.registrarEntrada("xyz98k")).isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    @DisplayName("cuposDisponibles() nunca es negativo")
    void cuposNuncaNegativos() {
        when(registroRepository.countByHoraSalidaIsNull()).thenReturn(10L);

        assertThat(service.cuposDisponibles()).isZero();
    }

    @Test
    @DisplayName("registrarSalida() marca la hora de salida")
    void registrarSalida() {
        RegistroVehiculoEntity dentro = RegistroVehiculoEntity.builder().id(4L).placa("ABC123")
                .horaEntrada(LocalDateTime.now().minusMinutes(45)).build();
        when(registroRepository.findById(4L)).thenReturn(Optional.of(dentro));
        when(registroRepository.existsById(4L)).thenReturn(true);
        guardarDevuelveLoMismo();

        RegistroVehiculo registro = service.registrarSalida(4L);

        assertThat(registro.estaActivo()).isFalse();
        assertThat(registro.minutosEstacionado()).isBetween(44L, 46L);
    }

    @Test
    @DisplayName("registrarSalida() dos veces lanza EstadoInvalidoException")
    void registrarSalidaDosVeces() {
        RegistroVehiculoEntity yaSalio = RegistroVehiculoEntity.builder().id(4L).placa("ABC123")
                .horaEntrada(LocalDateTime.now().minusHours(2)).horaSalida(LocalDateTime.now().minusHours(1)).build();
        when(registroRepository.findById(4L)).thenReturn(Optional.of(yaSalio));

        assertThatThrownBy(() -> service.registrarSalida(4L)).isInstanceOf(EstadoInvalidoException.class);
    }
}
