package com.restaurante.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.mapper.PlatoEntityMapper;
import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.entity.PlatoEntity;
import com.restaurante.persistence.repository.PlatoJpaRepository;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock
    private PlatoJpaRepository platoRepository;

    @Mock
    private PlatoEntityMapper entityMapper;

    @InjectMocks
    private PlatoServiceImpl service;

    private Plato platoDominio(Long id, String nombre, String categoria, boolean disponible) {
        return Plato.builder()
                .id(id)
                .nombre(nombre)
                .precio(25000.0)
                .categoria(categoria)
                .descripcion("Descripcion de prueba")
                .disponible(disponible)
                .build();
    }

    private PlatoEntity platoEntity(Long id, String nombre, String categoria, boolean disponible) {
        return PlatoEntity.builder()
                .id(id)
                .nombre(nombre)
                .precio(25000.0)
                .categoria(categoria)
                .descripcion("Descripcion de prueba")
                .disponible(disponible)
                .build();
    }

    @Test
    @DisplayName("obtenerTodos con la lista vacia devuelve lista vacia, no null")
    void obtenerTodos_listaVacia_debeRetornarListaVacia() {
        when(platoRepository.findAll()).thenReturn(List.of());

        List<Plato> resultado = service.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("obtenerTodos devuelve todos los platos creados")
    void obtenerTodos_debeRetornarTodosLosPlatos() {
        PlatoEntity e1 = platoEntity(1L, "Roll California", "Roll", true);
        PlatoEntity e2 = platoEntity(2L, "Sashimi de atun", "Sashimi", true);
        when(platoRepository.findAll()).thenReturn(List.of(e1, e2));
        when(entityMapper.toDomain(e1)).thenReturn(platoDominio(1L, "Roll California", "Roll", true));
        when(entityMapper.toDomain(e2)).thenReturn(platoDominio(2L, "Sashimi de atun", "Sashimi", true));

        List<Plato> resultado = service.obtenerTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("obtenerPorId con id existente devuelve el plato")
    void obtenerPorId_existente_debeRetornarPlato() {
        PlatoEntity entity = platoEntity(1L, "Roll California", "Roll", true);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(platoDominio(1L, "Roll California", "Roll", true));

        Plato resultado = service.obtenerPorId(1L);

        assertEquals("Roll California", resultado.getNombre());
    }

    @Test
    @DisplayName("obtenerPorId con id inexistente lanza RecursoNoEncontradoException")
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        when(platoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(999L));
    }

    @Test
    @DisplayName("crear asigna un id y guarda el plato")
    void crear_debeAsignarIdYGuardar() {
        Plato nuevo = platoDominio(null, "Nigiri de salmon", "Nigiri", true);
        PlatoEntity entitySinId = platoEntity(null, "Nigiri de salmon", "Nigiri", true);
        PlatoEntity entityGuardada = platoEntity(1L, "Nigiri de salmon", "Nigiri", true);
        when(platoRepository.existsByNombreIgnoreCase("Nigiri de salmon")).thenReturn(false);
        when(entityMapper.toEntity(nuevo)).thenReturn(entitySinId);
        when(platoRepository.save(entitySinId)).thenReturn(entityGuardada);
        when(entityMapper.toDomain(entityGuardada)).thenReturn(platoDominio(1L, "Nigiri de salmon", "Nigiri", true));

        Plato resultado = service.crear(nuevo);

        assertNotNull(resultado.getId());
        verify(platoRepository, times(1)).save(entitySinId);
    }

    @Test
    @DisplayName("crear con nombre ya existente lanza ConflictoException")
    void crear_nombreDuplicado_debeLanzarConflicto() {
        Plato nuevo = platoDominio(null, "Roll California", "Roll", true);
        when(platoRepository.existsByNombreIgnoreCase("Roll California")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.crear(nuevo));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar con id existente modifica los campos del plato")
    void actualizar_existente_debeActualizarCampos() {
        PlatoEntity existente = platoEntity(1L, "Roll California", "Roll", true);
        Plato nuevosDatos = platoDominio(null, "Roll California Especial", "Roll", true);
        nuevosDatos.setPrecio(28000.0);
        PlatoEntity actualizada = platoEntity(1L, "Roll California Especial", "Roll", true);
        actualizada.setPrecio(28000.0);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(platoRepository.existsByNombreIgnoreCaseAndIdNot("Roll California Especial", 1L)).thenReturn(false);
        when(platoRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(
                platoDominio(1L, "Roll California Especial", "Roll", true));

        Plato resultado = service.actualizar(1L, nuevosDatos);

        assertEquals("Roll California Especial", resultado.getNombre());
    }

    @Test
    @DisplayName("actualizar con id inexistente lanza RecursoNoEncontradoException")
    void actualizar_inexistente_debeLanzarExcepcion() {
        Plato nuevosDatos = platoDominio(null, "Roll X", "Roll", true);
        when(platoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(999L, nuevosDatos));
    }

    @Test
    @DisplayName("actualizar con nombre ya usado por otro plato lanza ConflictoException")
    void actualizar_nombreDuplicado_debeLanzarConflicto() {
        PlatoEntity existente = platoEntity(1L, "Roll California", "Roll", true);
        Plato nuevosDatos = platoDominio(null, "Sashimi mixto", "Sashimi", true);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(platoRepository.existsByNombreIgnoreCaseAndIdNot("Sashimi mixto", 1L)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.actualizar(1L, nuevosDatos));
    }

    @Test
    @DisplayName("cambiarDisponibilidad activa y desactiva el plato correctamente")
    void cambiarDisponibilidad_debeActualizarEstado() {
        PlatoEntity existente = platoEntity(1L, "Roll California", "Roll", true);
        PlatoEntity actualizada = platoEntity(1L, "Roll California", "Roll", false);
        when(platoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(platoRepository.save(existente)).thenReturn(actualizada);
        when(entityMapper.toDomain(actualizada)).thenReturn(platoDominio(1L, "Roll California", "Roll", false));

        Plato resultado = service.cambiarDisponibilidad(1L, false);

        assertFalse(resultado.estaDisponible());
    }

    @Test
    @DisplayName("cambiarDisponibilidad con id inexistente lanza RecursoNoEncontradoException")
    void cambiarDisponibilidad_inexistente_debeLanzarExcepcion() {
        when(platoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cambiarDisponibilidad(999L, true));
    }

    @Test
    @DisplayName("eliminar con id existente elimina el plato")
    void eliminar_existente_debeEliminarPlato() {
        when(platoRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(platoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar con id inexistente lanza RecursoNoEncontradoException")
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(platoRepository.existsById(999L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(999L));
        verify(platoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("obtenerDisponibles filtra solo los platos disponibles")
    void obtenerDisponibles_debeFiltrarSoloDisponibles() {
        PlatoEntity disponible = platoEntity(1L, "Roll California", "Roll", true);
        when(platoRepository.findByDisponible(true)).thenReturn(List.of(disponible));
        when(entityMapper.toDomain(disponible)).thenReturn(platoDominio(1L, "Roll California", "Roll", true));

        List<Plato> resultado = service.obtenerDisponibles();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    @DisplayName("obtenerPorCategoria filtra por categoria ignorando mayusculas")
    void obtenerPorCategoria_debeFiltrarPorCategoria() {
        PlatoEntity entity = platoEntity(1L, "Roll California", "Roll", true);
        when(platoRepository.findByCategoriaIgnoreCase("roll")).thenReturn(List.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(platoDominio(1L, "Roll California", "Roll", true));

        List<Plato> resultado = service.obtenerPorCategoria("roll");

        assertEquals(1, resultado.size());
        assertEquals("Roll California", resultado.get(0).getNombre());
    }
}
