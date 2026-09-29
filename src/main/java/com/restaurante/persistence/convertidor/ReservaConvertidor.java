package com.restaurante.persistence.convertidor;

import org.springframework.stereotype.Component;

import com.restaurante.model.domain.Reserva;
import com.restaurante.persistence.entity.ReservaEntity;

@Component
public class ReservaConvertidor implements ConvertidorEntidad<Reserva, ReservaEntity> {

    @Override
    public Reserva aDominio(ReservaEntity entidad) {
        return Reserva.builder()
                .id(entidad.getId())
                .idMesa(entidad.getIdMesa())
                .nombreCliente(entidad.getNombreCliente())
                .fechaHora(entidad.getFechaHora())
                .numeroPersonas(entidad.getNumeroPersonas())
                .cancelada(entidad.isCancelada())
                .build();
    }

    @Override
    public ReservaEntity aEntidad(Reserva dominio) {
        return ReservaEntity.builder()
                .id(dominio.getId())
                .idMesa(dominio.getIdMesa())
                .nombreCliente(dominio.getNombreCliente())
                .fechaHora(dominio.getFechaHora())
                .numeroPersonas(dominio.getNumeroPersonas())
                .cancelada(dominio.isCancelada())
                .build();
    }
}
