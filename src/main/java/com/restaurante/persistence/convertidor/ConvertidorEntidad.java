package com.restaurante.persistence.convertidor;

import java.util.List;

/**
 * Traduce entre el modelo de dominio (lo que usan los Services) y la entidad
 * JPA (lo que se guarda en PostgreSQL). Asi el dominio no queda amarrado a
 * anotaciones de base de datos.
 *
 * @param <D> clase de dominio (ej. Plato)
 * @param <E> entidad JPA (ej. PlatoEntity)
 */
public interface ConvertidorEntidad<D, E> {

    D aDominio(E entidad);

    E aEntidad(D dominio);

    default List<D> aDominioLista(List<E> entidades) {
        return entidades.stream().map(this::aDominio).toList();
    }
}
