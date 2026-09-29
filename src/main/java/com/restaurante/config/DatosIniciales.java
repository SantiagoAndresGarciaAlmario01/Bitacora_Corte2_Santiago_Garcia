package com.restaurante.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.model.domain.EstadoMesa;
import com.restaurante.model.domain.Rol;
import com.restaurante.persistence.entity.MesaEntity;
import com.restaurante.persistence.entity.PlatoEntity;
import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.persistence.repository.MesaRepository;
import com.restaurante.persistence.repository.PlatoRepository;
import com.restaurante.persistence.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Carga datos de arranque la primera vez que la app corre contra una base
 * vacia: un usuario por rol (para poder probar la seguridad), algunas mesas y
 * algunos platos del menu. Si las tablas ya tienen datos, no hace nada.
 *
 * <p>Las contrasenas son de desarrollo y estan documentadas en el README.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final PlatoRepository platoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.saveAll(List.of(
                    usuario("Gerente Kaze & Nori", "gerente@kazenori.co", "Gerente2026*", Rol.GERENTE),
                    usuario("Mesero de turno", "mesero@kazenori.co", "Mesero2026*", Rol.MESERO),
                    usuario("Chef de cocina", "cocina@kazenori.co", "Cocina2026*", Rol.COCINERO),
                    usuario("Cliente de prueba", "cliente@kazenori.co", "Cliente2026*", Rol.CLIENTE)));
            log.info("Usuarios iniciales creados (uno por rol)");
        }
        if (mesaRepository.count() == 0) {
            mesaRepository.saveAll(List.of(
                    mesa(1, 2), mesa(2, 2), mesa(3, 4), mesa(4, 4), mesa(5, 6), mesa(6, 8)));
            log.info("Mesas iniciales creadas");
        }
        if (platoRepository.count() == 0) {
            platoRepository.saveAll(List.of(
                    plato("Roll Acevichado", 32000.0, "Roll", "Langostino tempura, aguacate y salsa acevichada"),
                    plato("Nigiri de Salmon (2 und)", 16000.0, "Nigiri", "Arroz de sushi con salmon fresco"),
                    plato("Sashimi Mixto", 38000.0, "Sashimi", "Cortes de salmon, atun y pesca blanca"),
                    plato("Temaki Spicy Tuna", 24000.0, "Temaki", "Cono de alga con atun picante y pepino"),
                    plato("Gyozas de Cerdo", 18000.0, "Entrada", "Seis empanaditas japonesas a la plancha"),
                    plato("Te Verde Frio", 7000.0, "Bebida", "Te matcha con limonaria")));
            log.info("Platos iniciales creados");
        }
    }

    private UsuarioEntity usuario(String nombre, String correo, String contrasena, Rol rol) {
        return UsuarioEntity.builder()
                .nombre(nombre)
                .correo(correo)
                .contrasenaHash(passwordEncoder.encode(contrasena))
                .rol(rol)
                .build();
    }

    private static MesaEntity mesa(int numero, int capacidad) {
        return MesaEntity.builder().numero(numero).capacidad(capacidad).estado(EstadoMesa.LIBRE).build();
    }

    private static PlatoEntity plato(String nombre, double precio, String categoria, String descripcion) {
        return PlatoEntity.builder()
                .nombre(nombre)
                .precio(precio)
                .categoria(categoria)
                .disponible(true)
                .descripcion(descripcion)
                .build();
    }
}
