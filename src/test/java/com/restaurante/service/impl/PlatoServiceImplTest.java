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

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.Plato;
import com.restaurante.persistence.convertidor.PlatoConvertidor;
import com.restaurante.persistence.entity.PlatoEntity;
import com.restaurante.persistence.repository.PlatoRepository;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock
    private PlatoRepository platoRepository;

    private PlatoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlatoServiceImpl(platoRepository, new PlatoConvertidor());
    }

    private PlatoEntity entidad(Long id, String nombre, String categoria, boolean disponible) {
        return PlatoEntity.builder()
                .id(id)
                .nombre(nombre)
                .precio(25000.0)
                .categoria(categoria)
                .disponible(disponible)
                .descripcion("Descripcion de prueba")
                .build();
    }

    private void guardarDevuelveLoMismoConId() {
        when(platoRepository.save(any(PlatoEntity.class))).thenAnswer(inv -> {
            PlatoEntity e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(1L);
            }
            return e;
        });
    }

    @Test
    @DisplayName("crear() guarda en BD, asigna id y deja el plato disponible por defecto")
    void crearGuardaYQuedaDisponible() {
        guardarDevuelveLoMismoConId();
        Plato nuevo = Plato.builder().nombre("Roll Acevichado").precio(32000.0).categoria("Roll").build();

        Plato creado = service.crear(nuevo);

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.estaDisponible()).isTrue();
        verify(platoRepository).save(any(PlatoEntity.class));
    }

    @Test
    @DisplayName("crear() ignora el id que venga en el objeto")
    void crearIgnoraIdEntrante() {
        guardarDevuelveLoMismoConId();
        Plato conId = Plato.builder().id(77L).nombre("Gyozas").precio(18000.0).categoria("Entrada").build();

        Plato creado = service.crear(conId);

        assertThat(creado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("obtenerTodos() convierte todas las filas de la tabla")
    void obtenerTodosConvierteTodo() {
        when(platoRepository.findAll()).thenReturn(List.of(
                entidad(1L, "Roll Acevichado", "Roll", true),
                entidad(2L, "Sashimi Mixto", "Sashimi", false)));

        List<Plato> todos = service.obtenerTodos();

        assertThat(todos).extracting(Plato::getNombre).containsExactly("Roll Acevichado", "Sashimi Mixto");
    }

    @Test
    @DisplayName("obtenerTodos() con la tabla vacia devuelve lista vacia, no null")
    void obtenerTodosVacio() {
        when(platoRepository.findAll()).thenReturn(List.of());

        assertThat(service.obtenerTodos()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("obtenerDisponibles() usa la consulta de disponibles del repositorio")
    void obtenerDisponiblesDelegaEnRepositorio() {
        when(platoRepository.findByDisponibleTrue()).thenReturn(List.of(entidad(1L, "Roll Acevichado", "Roll", true)));

        List<Plato> disponibles = service.obtenerDisponibles();

        assertThat(disponibles).hasSize(1);
        assertThat(disponibles.get(0).estaDisponible()).isTrue();
    }

    @Test
    @DisplayName("obtenerPorCategoria() consulta sin importar mayusculas")
    void obtenerPorCategoriaDelegaEnRepositorio() {
        when(platoRepository.findByCategoriaIgnoreCase("nigiri"))
                .thenReturn(List.of(entidad(3L, "Nigiri de Salmon", "Nigiri", true)));

        List<Plato> nigiris = service.obtenerPorCategoria("nigiri");

        assertThat(nigiris).extracting(Plato::getCategoria).containsExactly("Nigiri");
    }

    @Test
    @DisplayName("obtenerPorId() devuelve el plato cuando existe")
    void obtenerPorIdExistente() {
        when(platoRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, "Roll Acevichado", "Roll", true)));

        Plato plato = service.obtenerPorId(1L);

        assertThat(plato.getNombre()).isEqualTo("Roll Acevichado");
    }

    @Test
    @DisplayName("obtenerPorId() lanza RecursoNoEncontradoException cuando no existe")
    void obtenerPorIdInexistente() {
        when(platoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("actualizar() cambia los campos y conserva id y disponibilidad")
    void actualizarModificaCampos() {
        when(platoRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, "Roll Acevichado", "Roll", false)));
        when(platoRepository.existsById(1L)).thenReturn(true);
        guardarDevuelveLoMismoConId();
        Plato nuevosDatos = Plato.builder().nombre("Roll Acevichado Especial").precio(35000.0)
                .categoria("Roll").descripcion("Con extra de salsa").build();

        Plato actualizado = service.actualizar(1L, nuevosDatos);

        assertThat(actualizado.getId()).isEqualTo(1L);
        assertThat(actualizado.getNombre()).isEqualTo("Roll Acevichado Especial");
        assertThat(actualizado.getPrecio()).isEqualTo(35000.0);
        assertThat(actualizado.estaDisponible()).isFalse();
    }

    @Test
    @DisplayName("actualizar() con id inexistente lanza excepcion y no guarda")
    void actualizarInexistente() {
        when(platoRepository.findById(999L)).thenReturn(Optional.empty());
        Plato nuevosDatos = Plato.builder().nombre("X").precio(1.0).categoria("Roll").build();

        assertThatThrownBy(() -> service.actualizar(999L, nuevosDatos))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(platoRepository, never()).save(any(PlatoEntity.class));
    }

    @Test
    @DisplayName("cambiarDisponibilidad() desactiva un plato y lo guarda")
    void cambiarDisponibilidadDesactiva() {
        when(platoRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, "Roll Acevichado", "Roll", true)));
        when(platoRepository.existsById(1L)).thenReturn(true);
        guardarDevuelveLoMismoConId();

        Plato plato = service.cambiarDisponibilidad(1L, false);

        assertThat(plato.estaDisponible()).isFalse();
    }

    @Test
    @DisplayName("eliminar() borra el plato cuando existe")
    void eliminarExistente() {
        when(platoRepository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(platoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar() con id inexistente lanza excepcion y no borra nada")
    void eliminarInexistente() {
        when(platoRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(999L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(platoRepository, never()).deleteById(anyLong());
    }
}
