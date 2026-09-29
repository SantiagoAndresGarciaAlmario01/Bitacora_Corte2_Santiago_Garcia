package com.restaurante.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.persistence.convertidor.ConvertidorEntidad;

import lombok.extern.slf4j.Slf4j;

/**
 * Base generica para los servicios CRUD del restaurante.
 *
 * <p>En la version de la Semana 8 esta clase guardaba todo en un {@code Map}
 * en memoria. Ahora el almacenamiento real es PostgreSQL: la clase recibe el
 * repositorio JPA de la entidad y un {@link ConvertidorEntidad} para pasar de
 * entidad a dominio y viceversa. Las operaciones que ofrece a las subclases
 * (listar, buscar, guardar, reemplazar, eliminar) son las mismas de antes, asi
 * que cada servicio concreto sigue concentrado solo en sus reglas de negocio.</p>
 *
 * @param <D> clase de dominio (ej. Plato)
 * @param <E> entidad JPA correspondiente (ej. PlatoEntity)
 */
@Slf4j
public abstract class AbstractCrudServiceImpl<D, E> {

    /** Repositorio JPA de la entidad. */
    protected abstract JpaRepository<E, Long> repositorio();

    /** Convertidor entre la entidad JPA y el dominio. */
    protected abstract ConvertidorEntidad<D, E> convertidor();

    /** Asigna el id a un objeto de dominio (se usa al reemplazar). */
    protected abstract void asignarId(D dominio, Long id);

    /** Nombre legible del recurso para mensajes de error (ej. "Plato"). */
    protected abstract String nombreRecurso();

    protected List<D> listarTodos() {
        return convertidor().aDominioLista(repositorio().findAll());
    }

    protected List<D> aDominio(List<E> entidades) {
        return convertidor().aDominioLista(entidades);
    }

    protected E buscarEntidad(Long id) {
        return repositorio().findById(id)
                .orElseThrow(() -> {
                    log.warn("{} no encontrado: id={}", nombreRecurso(), id);
                    return new RecursoNoEncontradoException(nombreRecurso(), id);
                });
    }

    protected D buscarPorId(Long id) {
        return convertidor().aDominio(buscarEntidad(id));
    }

    protected boolean existe(Long id) {
        return repositorio().existsById(id);
    }

    /** Inserta un objeto nuevo (se ignora cualquier id que traiga). */
    protected D guardar(D dominio) {
        asignarId(dominio, null);
        E guardada = repositorio().save(convertidor().aEntidad(dominio));
        D resultado = convertidor().aDominio(guardada);
        log.info("{} guardado en BD", nombreRecurso());
        return resultado;
    }

    /** Actualiza un objeto que ya existe (404 si no existe). */
    protected D reemplazar(Long id, D dominio) {
        if (!existe(id)) {
            throw new RecursoNoEncontradoException(nombreRecurso(), id);
        }
        asignarId(dominio, id);
        E guardada = repositorio().save(convertidor().aEntidad(dominio));
        log.info("{} actualizado en BD: id={}", nombreRecurso(), id);
        return convertidor().aDominio(guardada);
    }

    protected void eliminarPorId(Long id) {
        if (!existe(id)) {
            log.warn("{} no encontrado para eliminar: id={}", nombreRecurso(), id);
            throw new RecursoNoEncontradoException(nombreRecurso(), id);
        }
        repositorio().deleteById(id);
        log.info("{} eliminado de BD: id={}", nombreRecurso(), id);
    }
}
