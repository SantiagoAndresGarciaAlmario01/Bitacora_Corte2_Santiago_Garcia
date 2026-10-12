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

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.MesaEntityMapper;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Mesa;
import com.restaurante.persistence.entity.MesaEntity;
import com.restaurante.persistence.repository.MesaJpaRepository;

@ExtendWith(MockitoExtension.class)
class MesaServiceImplTest {

    @Mock
    private MesaJpaRepository mesaRepository;

    @Mock
    private MesaEntityMapper entityMapper;

    @InjectMocks
    private MesaServiceImpl service;

    private Mesa mesaDominio(Long id, int numero, int capacidad, EstadoMesa estado) {
        return Mesa.builder().id(id).numero(numero).capacidad(capacidad).estado(estado).build();
    }

    private MesaEntity mesaEntity(Long id, int numero, int capacidad, EstadoMesa estado) {
        return MesaEntity.builder().id(id).numero(numero).capacidad(capacidad).estado(estado).build();
    }

    @Test
    @DisplayName("obtenerTodos con la lista vacia devuelve lista vacia, no null")
    void obtenerTodos_listaVacia_debeRetornarListaVacia() {
        when(mesaRepository.findAll()).thenReturn(List.of());

        List<Mesa> resultado = service.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("crear asigna un id y guarda la mesa")
    void crear_debeAsignarIdYGuardar() {
        Mesa nueva = mesaDominio(null, 1, 4, EstadoMesa.LIBRE);
        MesaEntity entitySinId = mesaEntity(null, 1, 4, EstadoMesa.LIBRE);
        MesaEntity entityGuardada = mesaEntity(1L, 1, 4, EstadoMesa.LIBRE);
        when(entityMapper.toEntity(nueva)).thenReturn(entitySinId);
        when(mesaRepository.save(entitySinId)).thenReturn(entityGuardada);
        when(entityMapper.toDomain(entityGuardada)).thenReturn(mesaDominio(1L, 1, 4, EstadoMesa.LIBRE));

        Mesa resultado = service.crear(nueva);

        assertNotNull(resultado.getId());
    }

    @Test
    @DisplayName("obtenerPorId con id existente devuelve la mesa")
    void obtenerPorId_existente_debeRetornarMesa() {
        MesaEntity entity = mesaEntity(2L, 2, 6, EstadoMesa.LIBRE);
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(mesaDominio(2L, 2, 6, EstadoMesa.LIBRE));

        Mesa resultado = service.obtenerPorId(2L);

        assertEquals(2, resultado.getNumero());
    }

    @Test
    @DisplayName("obtenerPorId con id inexistente lanza RecursoNoEncontradoException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        when(mesaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(999L));
    }

    @Test
    @DisplayName("actualizar con id existente modifica los campos de la mesa")
    void actualizar_existente_debeActualizarCampos() {
        MesaEntity existente = mesaEntity(3L, 3, 4, EstadoMesa.LIBRE);
        Mesa nuevosDatos = mesaDominio(null, 3, 8, EstadoMesa.LIBRE);
        MesaEntity actualizada = mesaEntity(3L, 3, 8, EstadoMesa.LIBRE);
        when(mesaRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(mesaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(mesaDominio(3L, 3, 8, EstadoMesa.LIBRE));

        Mesa resultado = service.actualizar(3L, nuevosDatos);

        assertEquals(8, resultado.getCapacidad());
    }

    @Test
    @DisplayName("actualizar con id inexistente lanza RecursoNoEncontradoException")
    void actualizar_inexistente_debeLanzarExcepcion() {
        Mesa nuevosDatos = mesaDominio(null, 1, 4, EstadoMesa.LIBRE);
        when(mesaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(999L, nuevosDatos));
    }

    @Test
    @DisplayName("cambiarEstado ocupa y libera la mesa correctamente")
    void cambiarEstado_debeActualizarEstado() {
        MesaEntity existente = mesaEntity(4L, 4, 4, EstadoMesa.LIBRE);
        MesaEntity actualizada = mesaEntity(4L, 4, 4, EstadoMesa.OCUPADA);
        when(mesaRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(mesaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(mesaDominio(4L, 4, 4, EstadoMesa.OCUPADA));

        Mesa resultado = service.cambiarEstado(4L, EstadoMesa.OCUPADA);

        assertEquals(EstadoMesa.OCUPADA, resultado.getEstado());
    }

    @Test
    @DisplayName("cambiarEstado a RESERVADA tambien actualiza el estado")
    void cambiarEstado_reservada_debeActualizarEstado() {
        MesaEntity existente = mesaEntity(5L, 5, 4, EstadoMesa.LIBRE);
        MesaEntity actualizada = mesaEntity(5L, 5, 4, EstadoMesa.RESERVADA);
        when(mesaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(mesaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(mesaDominio(5L, 5, 4, EstadoMesa.RESERVADA));

        Mesa resultado = service.cambiarEstado(5L, EstadoMesa.RESERVADA);

        assertEquals(EstadoMesa.RESERVADA, resultado.getEstado());
    }

    @Test
    @DisplayName("cambiarEstado con id inexistente lanza RecursoNoEncontradoException")
    void cambiarEstado_inexistente_debeLanzarExcepcion() {
        when(mesaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cambiarEstado(999L, EstadoMesa.LIBRE));
    }

    @Test
    @DisplayName("eliminar con id existente elimina la mesa")
    void eliminar_existente_debeEliminarMesa() {
        when(mesaRepository.existsById(6L)).thenReturn(true);

        service.eliminar(6L);

        verify(mesaRepository, times(1)).deleteById(6L);
    }

    @Test
    @DisplayName("eliminar con id inexistente lanza RecursoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(mesaRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(999L));
        verify(mesaRepository, never()).deleteById(anyLong());
    }
}
