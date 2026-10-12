package com.restaurante.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.ReservaEntityMapper;
import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.entity.ReservaEntity;
import com.restaurante.persistence.repository.ReservaJpaRepository;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaJpaRepository reservaRepository;

    @Mock
    private ReservaEntityMapper entityMapper;

    @InjectMocks
    private ReservaServiceImpl service;

    private static final LocalDateTime FECHA = LocalDateTime.now(ZoneId.of("America/Bogota")).plusDays(1);

    private Reserva reservaDominio(Long id, Long idMesa, String cliente, LocalDateTime fecha, int personas,
            boolean cancelada) {
        return Reserva.builder()
                .id(id)
                .idMesa(idMesa)
                .nombreCliente(cliente)
                .fechaHora(fecha)
                .numeroPersonas(personas)
                .cancelada(cancelada)
                .build();
    }

    private ReservaEntity reservaEntity(Long id, Long idMesa, String cliente, LocalDateTime fecha, int personas,
            boolean cancelada) {
        return ReservaEntity.builder()
                .id(id)
                .idMesa(idMesa)
                .nombreCliente(cliente)
                .fechaHora(fecha)
                .numeroPersonas(personas)
                .cancelada(cancelada)
                .build();
    }

    @Test
    @DisplayName("obtenerTodos con la lista vacia devuelve lista vacia, no null")
    void obtenerTodos_listaVacia_debeRetornarListaVacia() {
        when(reservaRepository.findAll()).thenReturn(List.of());

        List<Reserva> resultado = service.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("crear asigna un id y guarda la reserva")
    void crear_debeAsignarIdYGuardar() {
        Reserva nueva = reservaDominio(null, 1L, "Juan Perez", FECHA, 2, false);
        ReservaEntity entitySinId = reservaEntity(null, 1L, "Juan Perez", FECHA, 2, false);
        ReservaEntity entityGuardada = reservaEntity(1L, 1L, "Juan Perez", FECHA, 2, false);
        when(entityMapper.toEntity(nueva)).thenReturn(entitySinId);
        when(reservaRepository.save(entitySinId)).thenReturn(entityGuardada);
        when(entityMapper.toDomain(entityGuardada))
                .thenReturn(reservaDominio(1L, 1L, "Juan Perez", FECHA, 2, false));

        Reserva resultado = service.crear(nueva);

        assertNotNull(resultado.getId());
    }

    @Test
    @DisplayName("obtenerPorId con id inexistente lanza RecursoNoEncontradoException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(999L));
    }

    @Test
    @DisplayName("obtenerPorMesa filtra las reservas de esa mesa")
    void obtenerPorMesa_debeFiltrarPorMesa() {
        ReservaEntity entity = reservaEntity(2L, 2L, "Cliente B", FECHA, 2, false);
        when(reservaRepository.findByIdMesa(2L)).thenReturn(List.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(reservaDominio(2L, 2L, "Cliente B", FECHA, 2, false));

        List<Reserva> resultado = service.obtenerPorMesa(2L);

        assertEquals(1, resultado.size());
        assertEquals("Cliente B", resultado.get(0).getNombreCliente());
    }

    @Test
    @DisplayName("actualizar modifica los campos de la reserva")
    void actualizar_existente_debeActualizarCampos() {
        ReservaEntity existente = reservaEntity(1L, 1L, "Cliente A", FECHA, 2, false);
        Reserva nuevosDatos = reservaDominio(null, 1L, "Cliente A Actualizado", FECHA, 5, false);
        ReservaEntity actualizada = reservaEntity(1L, 1L, "Cliente A Actualizado", FECHA, 5, false);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada))
                .thenReturn(reservaDominio(1L, 1L, "Cliente A Actualizado", FECHA, 5, false));

        Reserva resultado = service.actualizar(1L, nuevosDatos);

        assertEquals("Cliente A Actualizado", resultado.getNombreCliente());
        assertEquals(5, resultado.getNumeroPersonas());
    }

    @Test
    @DisplayName("actualizar con id inexistente lanza RecursoNoEncontradoException")
    void actualizar_inexistente_debeLanzarExcepcion() {
        Reserva nuevosDatos = reservaDominio(null, 1L, "Cliente X", FECHA, 2, false);
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(999L, nuevosDatos));
    }

    @Test
    @DisplayName("cancelar marca la reserva como cancelada")
    void cancelar_debeMarcarComoCancelada() {
        ReservaEntity existente = reservaEntity(1L, 1L, "Cliente A", FECHA, 2, false);
        ReservaEntity actualizada = reservaEntity(1L, 1L, "Cliente A", FECHA, 2, true);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(reservaDominio(1L, 1L, "Cliente A", FECHA, 2, true));

        Reserva resultado = service.cancelar(1L);

        assertTrue(resultado.isCancelada());
    }

    @Test
    @DisplayName("cancelar con id inexistente lanza RecursoNoEncontradoException")
    void cancelar_inexistente_debeLanzarExcepcion() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cancelar(999L));
    }

    @Test
    @DisplayName("reprogramar cambia la fecha y hora de la reserva")
    void reprogramar_debeCambiarFechaHora() {
        LocalDateTime nuevaFecha = FECHA.plusDays(4);
        ReservaEntity existente = reservaEntity(1L, 1L, "Cliente A", FECHA, 2, false);
        ReservaEntity actualizada = reservaEntity(1L, 1L, "Cliente A", nuevaFecha, 2, false);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada))
                .thenReturn(reservaDominio(1L, 1L, "Cliente A", nuevaFecha, 2, false));

        Reserva resultado = service.reprogramar(1L, nuevaFecha);

        assertEquals(nuevaFecha, resultado.getFechaHora());
    }

    @Test
    @DisplayName("reprogramar con id inexistente lanza RecursoNoEncontradoException")
    void reprogramar_inexistente_debeLanzarExcepcion() {
        LocalDateTime nuevaFecha = FECHA.plusDays(5);
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.reprogramar(999L, nuevaFecha));
    }

    @Test
    @DisplayName("eliminar con id existente elimina la reserva")
    void eliminar_existente_debeEliminarReserva() {
        when(reservaRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(reservaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar con id inexistente lanza RecursoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(reservaRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(999L));
        verify(reservaRepository, never()).deleteById(anyLong());
    }
}
