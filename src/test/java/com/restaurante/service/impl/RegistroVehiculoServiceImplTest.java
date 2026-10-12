package com.restaurante.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.RegistroVehiculoEntityMapper;
import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.persistence.entity.RegistroVehiculoEntity;
import com.restaurante.persistence.repository.RegistroVehiculoJpaRepository;

@ExtendWith(MockitoExtension.class)
class RegistroVehiculoServiceImplTest {

    @Mock
    private RegistroVehiculoJpaRepository registroRepository;

    @Mock
    private RegistroVehiculoEntityMapper entityMapper;

    @InjectMocks
    private RegistroVehiculoServiceImpl service;

    private static final LocalDateTime ENTRADA = LocalDateTime.now();

    private RegistroVehiculo registroDominio(Long id, String placa, LocalDateTime entrada, LocalDateTime salida) {
        return RegistroVehiculo.builder().id(id).placa(placa).horaEntrada(entrada).horaSalida(salida).build();
    }

    private RegistroVehiculoEntity registroEntity(Long id, String placa, LocalDateTime entrada,
            LocalDateTime salida) {
        return RegistroVehiculoEntity.builder().id(id).placa(placa).horaEntrada(entrada).horaSalida(salida).build();
    }

    @Test
    @DisplayName("obtenerTodos con la lista vacia devuelve lista vacia, no null")
    void obtenerTodos_listaVacia_debeRetornarListaVacia() {
        when(registroRepository.findAll()).thenReturn(List.of());

        List<RegistroVehiculo> resultado = service.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("crear asigna un id y guarda el registro")
    void crear_debeAsignarIdYGuardar() {
        RegistroVehiculo nuevo = registroDominio(null, "ABC123", ENTRADA, null);
        RegistroVehiculoEntity entitySinId = registroEntity(null, "ABC123", ENTRADA, null);
        RegistroVehiculoEntity entityGuardada = registroEntity(1L, "ABC123", ENTRADA, null);
        when(entityMapper.toEntity(nuevo)).thenReturn(entitySinId);
        when(registroRepository.save(entitySinId)).thenReturn(entityGuardada);
        when(entityMapper.toDomain(entityGuardada)).thenReturn(registroDominio(1L, "ABC123", ENTRADA, null));

        RegistroVehiculo resultado = service.crear(nuevo);

        assertNotNull(resultado.getId());
    }

    @Test
    @DisplayName("obtenerPorId con id inexistente lanza RecursoNoEncontradoException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        when(registroRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(999L));
    }

    @Test
    @DisplayName("obtenerActivos filtra solo los que no tienen salida registrada")
    void obtenerActivos_debeFiltrarSoloActivos() {
        RegistroVehiculoEntity activo = registroEntity(1L, "AAA111", ENTRADA, null);
        when(registroRepository.findByHoraSalidaIsNull()).thenReturn(List.of(activo));
        when(entityMapper.toDomain(activo)).thenReturn(registroDominio(1L, "AAA111", ENTRADA, null));

        List<RegistroVehiculo> resultado = service.obtenerActivos();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    @DisplayName("registrarSalida asigna la hora de salida")
    void registrarSalida_debeAsignarHoraSalida() {
        RegistroVehiculoEntity existente = registroEntity(1L, "CCC333", ENTRADA, null);
        RegistroVehiculoEntity actualizado = registroEntity(1L, "CCC333", ENTRADA, LocalDateTime.now());
        when(registroRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(registroRepository.save(existente)).thenReturn(actualizado);
        when(entityMapper.toDomain(actualizado))
                .thenReturn(registroDominio(1L, "CCC333", ENTRADA, LocalDateTime.now()));

        RegistroVehiculo resultado = service.registrarSalida(1L);

        assertNotNull(resultado.getHoraSalida());
        assertFalse(resultado.estaActivo());
    }

    @Test
    @DisplayName("registrarSalida con id inexistente lanza RecursoNoEncontradoException")
    void registrarSalida_inexistente_debeLanzarExcepcion() {
        when(registroRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.registrarSalida(999L));
    }

    @Test
    @DisplayName("eliminar con id existente elimina el registro")
    void eliminar_existente_debeEliminarRegistro() {
        when(registroRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(registroRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar con id inexistente lanza RecursoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(registroRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(999L));
        verify(registroRepository, never()).deleteById(anyLong());
    }
}
