package com.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.model.domain.Plato;

class PlatoServiceImplTest {

    private PlatoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlatoServiceImpl();
    }

    private Plato platoDeEjemplo(String nombre, String categoria) {
        return Plato.builder()
                .nombre(nombre)
                .precio(25000.0)
                .categoria(categoria)
                .disponible(true)
                .descripcion("Descripcion de prueba")
                .build();
    }

    @Test
    @DisplayName("crear() asigna un id autoincremental y deja el plato disponible")
    void crearAsignaIdYDisponibilidad() {
        Plato creado = service.crear(platoDeEjemplo("Roll California", "Roll"));

        assertThat(creado.getId()).isEqualTo(1L);
        assertThat(creado.getNombre()).isEqualTo("Roll California");
    }

    @Test
    @DisplayName("crear() incrementa el id en cada llamada")
    void crearIncrementaIdEnCadaLlamada() {
        Plato primero = service.crear(platoDeEjemplo("Roll California", "Roll"));
        Plato segundo = service.crear(platoDeEjemplo("Nigiri de salmon", "Nigiri"));

        assertThat(primero.getId()).isEqualTo(1L);
        assertThat(segundo.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("obtenerTodos() devuelve todos los platos, disponibles o no")
    void obtenerTodosDevuelveTodo() {
        service.crear(platoDeEjemplo("Roll California", "Roll"));
        Plato segundo = service.crear(platoDeEjemplo("Nigiri de salmon", "Nigiri"));
        service.cambiarDisponibilidad(segundo.getId(), false);

        List<Plato> todos = service.obtenerTodos();

        assertThat(todos).hasSize(2);
    }

    @Test
    @DisplayName("obtenerDisponibles() solo devuelve los platos activos")
    void obtenerDisponiblesFiltraPorDisponibilidad() {
        Plato disponible = service.crear(platoDeEjemplo("Roll California", "Roll"));
        Plato noDisponible = service.crear(platoDeEjemplo("Nigiri de salmon", "Nigiri"));
        service.cambiarDisponibilidad(noDisponible.getId(), false);

        List<Plato> disponibles = service.obtenerDisponibles();

        assertThat(disponibles).extracting(Plato::getId).containsExactly(disponible.getId());
    }

    @Test
    @DisplayName("obtenerPorCategoria() filtra sin importar mayusculas/minusculas")
    void obtenerPorCategoriaEsCaseInsensitive() {
        service.crear(platoDeEjemplo("Roll California", "Roll"));
        service.crear(platoDeEjemplo("Nigiri de salmon", "Nigiri"));

        List<Plato> rolls = service.obtenerPorCategoria("roll");

        assertThat(rolls).hasSize(1);
        assertThat(rolls.get(0).getNombre()).isEqualTo("Roll California");
    }

    @Test
    @DisplayName("obtenerPorId() devuelve el plato cuando existe")
    void obtenerPorIdExistente() {
        Plato creado = service.crear(platoDeEjemplo("Roll California", "Roll"));

        Plato encontrado = service.obtenerPorId(creado.getId());

        assertThat(encontrado).isEqualTo(creado);
    }

    @Test
    @DisplayName("obtenerPorId() lanza RecursoNoEncontradoException cuando el id no existe")
    void obtenerPorIdInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> service.obtenerPorId(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("actualizar() reemplaza los campos del plato existente")
    void actualizarModificaCamposExistentes() {
        Plato creado = service.crear(platoDeEjemplo("Roll California", "Roll"));
        Plato nuevosDatos = platoDeEjemplo("Roll California Especial", "Roll");
        nuevosDatos.setPrecio(28000.0);

        Plato actualizado = service.actualizar(creado.getId(), nuevosDatos);

        assertThat(actualizado.getNombre()).isEqualTo("Roll California Especial");
        assertThat(actualizado.getPrecio()).isEqualTo(28000.0);
        assertThat(actualizado.getId()).isEqualTo(creado.getId());
    }

    @Test
    @DisplayName("actualizar() lanza RecursoNoEncontradoException si el id no existe")
    void actualizarInexistenteLanzaExcepcion() {
        Plato nuevosDatos = platoDeEjemplo("Roll California", "Roll");

        assertThatThrownBy(() -> service.actualizar(999L, nuevosDatos))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("cambiarDisponibilidad() activa y desactiva correctamente el plato")
    void cambiarDisponibilidadActivaYDesactiva() {
        Plato creado = service.crear(platoDeEjemplo("Roll California", "Roll"));

        Plato desactivado = service.cambiarDisponibilidad(creado.getId(), false);
        assertThat(desactivado.estaDisponible()).isFalse();

        Plato reactivado = service.cambiarDisponibilidad(creado.getId(), true);
        assertThat(reactivado.estaDisponible()).isTrue();
    }

    @Test
    @DisplayName("eliminar() borra el plato y una segunda eliminacion lanza RecursoNoEncontradoException")
    void eliminarBorraElPlatoYFallaSiSeRepite() {
        Plato creado = service.crear(platoDeEjemplo("Roll California", "Roll"));

        service.eliminar(creado.getId());

        assertThat(service.obtenerTodos()).isEmpty();
        assertThatThrownBy(() -> service.eliminar(creado.getId()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}