package com.restaurante.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.exception.ConflictoException;
import com.restaurante.exception.EstadoInvalidoException;
import com.restaurante.exception.ReglaDeNegocioException;
import com.restaurante.model.domain.RegistroVehiculo;
import com.restaurante.persistence.convertidor.ConvertidorEntidad;
import com.restaurante.persistence.convertidor.RegistroVehiculoConvertidor;
import com.restaurante.persistence.entity.RegistroVehiculoEntity;
import com.restaurante.persistence.repository.RegistroVehiculoRepository;
import com.restaurante.service.IRegistroVehiculoService;

import lombok.extern.slf4j.Slf4j;

/**
 * Parqueadero del restaurante:
 * <ul>
 *   <li>La placa se guarda en mayusculas y sin guion (ABC-123 -> ABC123).</li>
 *   <li>Un mismo vehiculo no puede tener dos entradas activas.</li>
 *   <li>No se aceptan entradas si no hay cupos
 *       ({@code restaurante.parqueadero.cupos}).</li>
 * </ul>
 */
@Service
@Slf4j
@Transactional
public class RegistroVehiculoServiceImpl extends AbstractCrudServiceImpl<RegistroVehiculo, RegistroVehiculoEntity>
        implements IRegistroVehiculoService {

    private final RegistroVehiculoRepository registroRepository;
    private final RegistroVehiculoConvertidor registroConvertidor;
    private final int cuposTotales;

    public RegistroVehiculoServiceImpl(RegistroVehiculoRepository registroRepository,
                                       RegistroVehiculoConvertidor registroConvertidor,
                                       @Value("${restaurante.parqueadero.cupos:15}") int cuposTotales) {
        this.registroRepository = registroRepository;
        this.registroConvertidor = registroConvertidor;
        this.cuposTotales = cuposTotales;
    }

    @Override
    protected JpaRepository<RegistroVehiculoEntity, Long> repositorio() {
        return registroRepository;
    }

    @Override
    protected ConvertidorEntidad<RegistroVehiculo, RegistroVehiculoEntity> convertidor() {
        return registroConvertidor;
    }

    @Override
    protected void asignarId(RegistroVehiculo dominio, Long id) {
        dominio.setId(id);
    }

    @Override
    protected String nombreRecurso() {
        return "Registro de parqueadero";
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroVehiculo> obtenerTodos() {
        return listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroVehiculo> obtenerActivos() {
        return aDominio(registroRepository.findByHoraSalidaIsNull());
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroVehiculo obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    @Override
    public RegistroVehiculo registrarEntrada(String placa) {
        String placaNormalizada = normalizarPlaca(placa);
        if (registroRepository.existsByPlacaAndHoraSalidaIsNull(placaNormalizada)) {
            throw new ConflictoException("El vehiculo " + placaNormalizada + " ya esta dentro del parqueadero");
        }
        if (cuposDisponibles() <= 0) {
            throw new ReglaDeNegocioException("El parqueadero esta lleno (" + cuposTotales + " cupos)");
        }
        RegistroVehiculo registro = RegistroVehiculo.builder()
                .placa(placaNormalizada)
                .horaEntrada(LocalDateTime.now())
                .build();
        return guardar(registro);
    }

    @Override
    public RegistroVehiculo registrarSalida(Long id) {
        RegistroVehiculo registro = buscarPorId(id);
        if (!registro.estaActivo()) {
            throw new EstadoInvalidoException("El vehiculo " + registro.getPlaca() + " ya habia salido");
        }
        registro.registrarSalida();
        log.info("Salida de {} despues de {} minutos", registro.getPlaca(), registro.minutosEstacionado());
        return reemplazar(id, registro);
    }

    @Override
    @Transactional(readOnly = true)
    public int cuposDisponibles() {
        long ocupados = registroRepository.countByHoraSalidaIsNull();
        return (int) Math.max(0, cuposTotales - ocupados);
    }

    @Override
    public void eliminar(Long id) {
        eliminarPorId(id);
    }

    static String normalizarPlaca(String placa) {
        return placa.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
    }
}
