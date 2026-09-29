package com.restaurante.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.exception.ReglaDeNegocioException;
import com.restaurante.model.domain.Cuenta;
import com.restaurante.model.domain.EstadoCuenta;
import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.Pedido;
import com.restaurante.persistence.convertidor.ConvertidorEntidad;
import com.restaurante.persistence.convertidor.CuentaConvertidor;
import com.restaurante.persistence.convertidor.PedidoConvertidor;
import com.restaurante.persistence.entity.CuentaEntity;
import com.restaurante.persistence.repository.CuentaRepository;
import com.restaurante.persistence.repository.PedidoRepository;
import com.restaurante.service.ICuentaService;
import com.restaurante.service.IMesaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo de una cuenta: se abre cuando llegan los clientes a la mesa (la mesa
 * pasa a OCUPADA), acumula los pedidos de esa visita y al cerrarse se cobra
 * solo lo ENTREGADO y la mesa vuelve a quedar LIBRE.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CuentaServiceImpl extends AbstractCrudServiceImpl<Cuenta, CuentaEntity> implements ICuentaService {

    private final CuentaRepository cuentaRepository;
    private final CuentaConvertidor cuentaConvertidor;
    private final PedidoRepository pedidoRepository;
    private final PedidoConvertidor pedidoConvertidor;
    private final IMesaService mesaService;

    @Override
    protected JpaRepository<CuentaEntity, Long> repositorio() {
        return cuentaRepository;
    }

    @Override
    protected ConvertidorEntidad<Cuenta, CuentaEntity> convertidor() {
        return cuentaConvertidor;
    }

    @Override
    protected void asignarId(Cuenta dominio, Long id) {
        dominio.setId(id);
    }

    @Override
    protected String nombreRecurso() {
        return "Cuenta";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> obtenerTodas() {
        return listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> obtenerPorEstado(EstadoCuenta estado) {
        return aDominio(cuentaRepository.findByEstado(estado));
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    @Override
    public Cuenta abrir(Long idMesa) {
        mesaService.obtenerPorId(idMesa);
        if (cuentaRepository.findFirstByIdMesaAndEstado(idMesa, EstadoCuenta.ABIERTA).isPresent()) {
            throw new ConflictoException("La mesa id=" + idMesa + " ya tiene una cuenta abierta");
        }
        Cuenta cuenta = Cuenta.builder().idMesa(idMesa).build();
        cuenta.abrir();
        Cuenta abierta = guardar(cuenta);
        mesaService.cambiarEstado(idMesa, EstadoMesa.OCUPADA);
        log.info("Cuenta abierta: id={} mesa={}", abierta.getId(), idMesa);
        return abierta;
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta obtenerAbiertaDeMesa(Long idMesa) {
        return cuentaRepository.findFirstByIdMesaAndEstado(idMesa, EstadoCuenta.ABIERTA)
                .map(cuentaConvertidor::aDominio)
                .orElseThrow(() -> new ReglaDeNegocioException(
                        "La mesa id=" + idMesa + " no tiene una cuenta abierta; abre una antes de pedir"));
    }

    @Override
    @Transactional(readOnly = true)
    public double calcularConsumoActual(Long idCuenta) {
        buscarEntidad(idCuenta);
        return pedidosDeLaCuenta(idCuenta).stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .mapToDouble(Pedido::calcularTotal)
                .sum();
    }

    @Override
    public Cuenta cerrar(Long id) {
        Cuenta cuenta = buscarPorId(id);
        if (!cuenta.estaAbierta()) {
            throw new EstadoInvalidoException("La cuenta id=" + id + " ya esta cerrada");
        }
        List<Pedido> pedidos = pedidosDeLaCuenta(id);
        boolean hayPendientes = pedidos.stream().anyMatch(p -> !p.estaCerrado());
        if (hayPendientes) {
            throw new ReglaDeNegocioException(
                    "No se puede cerrar la cuenta: hay pedidos que aun no se han entregado ni cancelado");
        }
        double total = pedidos.stream()
                .filter(Pedido::cuentaParaCobro)
                .mapToDouble(Pedido::calcularTotal)
                .sum();
        cuenta.cerrar(total);
        Cuenta cerrada = reemplazar(id, cuenta);
        mesaService.cambiarEstado(cuenta.getIdMesa(), EstadoMesa.LIBRE);
        log.info("Cuenta id={} cerrada. Total cobrado={}", id, total);
        return cerrada;
    }

    private List<Pedido> pedidosDeLaCuenta(Long idCuenta) {
        return pedidoConvertidor.aDominioLista(pedidoRepository.findByIdCuenta(idCuenta));
    }
}
