package com.restaurante.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.persistence.repository.UsuarioJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        usuarioRepository.save(usuario("gerente@sushicraft.com", "Gerente Sushi Craft", "GERENTE"));
        usuarioRepository.save(usuario("mesero@sushicraft.com", "Mesero Sushi Craft", "MESERO"));
        usuarioRepository.save(usuario("cocinero@sushicraft.com", "Cocinero Sushi Craft", "COCINERO"));
        usuarioRepository.save(usuario("cliente@sushicraft.com", "Cliente Sushi Craft", "CLIENTE"));

        log.info("Usuarios demo creados: gerente@sushicraft.com, mesero@sushicraft.com, "
                + "cocinero@sushicraft.com, cliente@sushicraft.com (password: sushicraft123)");
    }

    private UsuarioEntity usuario(String email, String nombre, String rol) {
        return UsuarioEntity.builder()
                .email(email)
                .password(passwordEncoder.encode("sushicraft123"))
                .nombre(nombre)
                .rol(rol)
                .build();
    }
}
