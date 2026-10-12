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
import com.restaurante.mapper.CuentaEntityMapper;
import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.persistence.entity.CuentaEntity;
import com.restaurante.persistence.repository.CuentaJpaRepository;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaJpaRepository cuentaRepository;

    @Mock
    private CuentaEntityMapper entityMapper;

    @InjectMocks
    private CuentaServiceImpl service;

    private Cuenta cuentaDominio(Long id, Long idMesa, EstadoCuenta estado, Double total) {
        return Cuenta.builder().id(id).idMesa(idMesa).estado(estado).total(total).build();
    }

    private CuentaEntity cuentaEntity(Long id, Long idMesa, EstadoCuenta estado, Double total) {
        return CuentaEntity.builder().id(id).idMesa(idMesa).estado(estado).total(total).build();
    }

    @Test
    @DisplayName("obtenerTodos con la lista vacia devuelve lista vacia, no null")
    void obtenerTodos_listaVacia_debeRetornarListaVacia() {
        when(cuentaRepository.findAll()).thenReturn(List.of());

        List<Cuenta> resultado = service.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("crear asigna un id y guarda la cuenta")
    void crear_debeAsignarIdYGuardar() {
        Cuenta nueva = cuentaDominio(null, 1L, EstadoCuenta.ABIERTA, 0.0);
        CuentaEntity entitySinId = cuentaEntity(null, 1L, EstadoCuenta.ABIERTA, 0.0);
        CuentaEntity entityGuardada = cuentaEntity(1L, 1L, EstadoCuenta.ABIERTA, 0.0);
        when(entityMapper.toEntity(nueva)).thenReturn(entitySinId);
        when(cuentaRepository.save(entitySinId)).thenReturn(entityGuardada);
        when(entityMapper.toDomain(entityGuardada)).thenReturn(cuentaDominio(1L, 1L, EstadoCuenta.ABIERTA, 0.0));

        Cuenta resultado = service.crear(nueva);

        assertNotNull(resultado.getId());
    }

    @Test
    @DisplayName("obtenerPorId con id existente devuelve la cuenta")
    void obtenerPorId_existente_debeRetornarCuenta() {
        CuentaEntity entity = cuentaEntity(2L, 2L, EstadoCuenta.ABIERTA, 0.0);
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(cuentaDominio(2L, 2L, EstadoCuenta.ABIERTA, 0.0));

        Cuenta resultado = service.obtenerPorId(2L);

        assertEquals(2L, resultado.getIdMesa());
    }

    @Test
    @DisplayName("obtenerPorId con id inexistente lanza RecursoNoEncontradoException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(999L));
    }

    @Test
    @DisplayName("obtenerPorMesa filtra las cuentas de esa mesa")
    void obtenerPorMesa_debeFiltrarPorMesa() {
        CuentaEntity entity = cuentaEntity(2L, 2L, EstadoCuenta.ABIERTA, 0.0);
        when(cuentaRepository.findByIdMesa(2L)).thenReturn(List.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(cuentaDominio(2L, 2L, EstadoCuenta.ABIERTA, 0.0));

        List<Cuenta> resultado = service.obtenerPorMesa(2L);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getIdMesa());
    }

    @Test
    @DisplayName("actualizarTotal cambia el total de la cuenta")
    void actualizarTotal_debeActualizarTotal() {
        CuentaEntity existente = cuentaEntity(3L, 3L, EstadoCuenta.ABIERTA, 0.0);
        CuentaEntity actualizada = cuentaEntity(3L, 3L, EstadoCuenta.ABIERTA, 75000.0);
        when(cuentaRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(cuentaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(cuentaDominio(3L, 3L, EstadoCuenta.ABIERTA, 75000.0));

        Cuenta resultado = service.actualizarTotal(3L, 75000.0);

        assertEquals(75000.0, resultado.getTotal());
    }

    @Test
    @DisplayName("actualizarTotal con id inexistente lanza RecursoNoEncontradoException")
    void actualizarTotal_inexistente_debeLanzarExcepcion() {
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizarTotal(999L, 1000.0));
    }

    @Test
    @DisplayName("cerrar cambia el estado a CERRADA")
    void cerrar_debeCambiarEstado() {
        CuentaEntity existente = cuentaEntity(4L, 4L, EstadoCuenta.ABIERTA, 0.0);
        CuentaEntity actualizada = cuentaEntity(4L, 4L, EstadoCuenta.CERRADA, 0.0);
        when(cuentaRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(cuentaRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(cuentaDominio(4L, 4L, EstadoCuenta.CERRADA, 0.0));

        Cuenta resultado = service.cerrar(4L);

        assertEquals(EstadoCuenta.CERRADA, resultado.getEstado());
    }

    @Test
    @DisplayName("cerrar con id inexistente lanza RecursoNoEncontradoException")
    void cerrar_inexistente_debeLanzarExcepcion() {
        when(cuentaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cerrar(999L));
    }

    @Test
    @DisplayName("eliminar con id existente elimina la cuenta")
    void eliminar_existente_debeEliminarCuenta() {
        when(cuentaRepository.existsById(5L)).thenReturn(true);

        service.eliminar(5L);

        verify(cuentaRepository, times(1)).deleteById(5L);
    }

    @Test
    @DisplayName("eliminar con id inexistente lanza RecursoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(cuentaRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(999L));
        verify(cuentaRepository, never()).deleteById(anyLong());
    }
}
