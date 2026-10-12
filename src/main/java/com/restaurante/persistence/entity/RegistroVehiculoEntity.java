package com.restaurante.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "registros_vehiculo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroVehiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String placa;

    @Column(nullable = false)
    private LocalDateTime horaEntrada;

    private LocalDateTime horaSalida;
}
