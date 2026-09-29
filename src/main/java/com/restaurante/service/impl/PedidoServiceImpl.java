package com.restaurante.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.exception.RecursoNoEncontradoException;
import com.restaurante.exception.ReglaDeNegocioException;
import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.ItemPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.model.domain.Plato;
import com.restaurante.model.domain.TipoEventoPedido;
import com.restaurante.persistence.convertidor.ConvertidorEntidad;
import com.restaurante.persistence.convertidor.PedidoConvertidor;
import com.restaurante.persistence.entity.PedidoEntity;
import com.restaurante.persistence.repository.PedidoRepository;
import com.restaurante.service.ICuentaService;
import com.restaurante.service.IHistorialPedidoService;
import com.restaurante.service.IPedidoService;
import com.restaurante.service.IPlatoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reglas de pedidos:
 * <ul>
 *   <li>Solo se puede pedir en una mesa que tenga una cuenta abierta.</li>
 *   <li>Solo se agregan platos disponibles, y solo mientras el pedido este en
 *       RECIBIDO o EN_PREPARACION.</li>
 *   <li>Los cambios de estado siguen el flujo definido en {@link EstadoPedido}.</li>
 *   <li>Cada cambio queda registrado en el historial (MongoDB).</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PedidoServiceImpl extends AbstractCrudServiceImpl<Pedido, PedidoEntity> implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoConvertidor pedidoConvertidor;
    private final ICuentaService cuentaService;
    private final IPlatoService platoService;
    private final IHistorialPedidoService historialService;

    @Override
    protected JpaRepository<PedidoEntity, Long> repositorio() {
        return pedidoRepository;
    }

    @Override
    protected ConvertidorEntidad<Pedido, PedidoEntity> convertidor() {
        return pedidoConvertidor;
    }

    @Override
    protected void asignarId(Pedido dominio, Long id) {
        dominio.setId(id);
    }

    @Override
    protected String nombreRecurso() {
        return "Pedido";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> obtenerTodos() {
        return listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPorMesa(Long idMesa) {
        return aDominio(pedidoRepository.findByIdMesaOrderByFechaCreacionAsc(idMesa));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPorEstado(EstadoPedido estado) {
        return aDominio(pedidoRepository.findByEstadoOrderByFechaCreacionAsc(estado));
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    @Override
    public Pedido crear(Long idMesa) {
        Cuenta cuenta = cuentaService.obtenerAbiertaDeMesa(idMesa);
        Pedido pedido = Pedido.builder()
                .idMesa(idMesa)
                .idCuenta(cuenta.getId())
                .estado(EstadoPedido.RECIBIDO)
                .fechaCreacion(LocalDateTime.now())
                .build();
        Pedido creado = guardar(pedido);
        historialService.registrar(creado.getId(), TipoEventoPedido.PEDIDO_CREADO, null,
                EstadoPedido.RECIBIDO, "Pedido creado para la mesa id=" + idMesa);
        return creado;
    }

    @Override
    public Pedido agregarItem(Long idPedido, Long idPlato, int cantidad) {
        PedidoEntity entidad = buscarEntidad(idPedido);
        validarQueSePuedeModificar(entidad);

        Plato plato = platoService.obtenerPorId(idPlato);
        if (!plato.estaDisponible()) {
            throw new ReglaDeNegocioException("El plato '" + plato.getNombre() + "' no esta disponible en este momento");
        }

        ItemPedido item = ItemPedido.builder()
                .idPlato(plato.getId())
                .nombrePlato(plato.getNombre())
                .precioUnitario(plato.getPrecio())
                .cantidad(cantidad)
                .build();
        entidad.agregarItem(pedidoConvertidor.itemAEntidad(item));
        Pedido actualizado = pedidoConvertidor.aDominio(pedidoRepository.saveAndFlush(entidad));

        historialService.registrar(idPedido, TipoEventoPedido.ITEM_AGREGADO, entidad.getEstado(), entidad.getEstado(),
                cantidad + " x " + plato.getNombre());
        return actualizado;
    }

    @Override
    public Pedido retirarItem(Long idPedido, Long idItem) {
        PedidoEntity entidad = buscarEntidad(idPedido);
        validarQueSePuedeModificar(entidad);

        if (!entidad.retirarItem(idItem)) {
            throw new RecursoNoEncontradoException("Item del pedido " + idPedido, idItem);
        }
        Pedido actualizado = pedidoConvertidor.aDominio(pedidoRepository.saveAndFlush(entidad));

        historialService.registrar(idPedido, TipoEventoPedido.ITEM_RETIRADO, entidad.getEstado(), entidad.getEstado(),
                "Se retiro el item id=" + idItem);
        return actualizado;
    }

    @Override
    public Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado) {
        PedidoEntity entidad = buscarEntidad(idPedido);
        EstadoPedido anterior = entidad.getEstado();

        if (!anterior.puedeCambiarA(nuevoEstado)) {
            throw new EstadoInvalidoException("Un pedido en " + anterior + " no puede pasar a " + nuevoEstado
                    + ". Permitidos: " + anterior.siguientesPermitidos());
        }
        if (nuevoEstado == EstadoPedido.EN_PREPARACION && entidad.getItems().isEmpty()) {
            throw new ReglaDeNegocioException("No se puede mandar a cocina un pedido sin items");
        }

        entidad.setEstado(nuevoEstado);
        Pedido actualizado = pedidoConvertidor.aDominio(pedidoRepository.save(entidad));
        historialService.registrar(idPedido, TipoEventoPedido.ESTADO_CAMBIADO, anterior, nuevoEstado,
                anterior + " -> " + nuevoEstado);
        log.info("Pedido id={} {} -> {}", idPedido, anterior, nuevoEstado);
        return actualizado;
    }

    @Override
    public void eliminar(Long id) {
        PedidoEntity entidad = buscarEntidad(id);
        EstadoPedido estado = entidad.getEstado();
        if (estado != EstadoPedido.RECIBIDO && estado != EstadoPedido.CANCELADO) {
            throw new EstadoInvalidoException("Solo se pueden eliminar pedidos en RECIBIDO o CANCELADO (actual: " + estado + ")");
        }
        eliminarPorId(id);
        historialService.registrar(id, TipoEventoPedido.PEDIDO_ELIMINADO, estado, null, "Pedido eliminado");
    }

    private void validarQueSePuedeModificar(PedidoEntity entidad) {
        Pedido pedido = pedidoConvertidor.aDominio(entidad);
        if (!pedido.puedeModificarse()) {
            throw new EstadoInvalidoException("El pedido id=" + entidad.getId() + " esta en " + entidad.getEstado()
                    + " y ya no se puede modificar");
        }
    }
}
