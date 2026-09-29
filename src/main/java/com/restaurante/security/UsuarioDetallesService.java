package com.restaurante.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.restaurante.persistence.entity.UsuarioEntity;
import com.restaurante.persistence.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Le dice a Spring Security como cargar un usuario de la tabla {@code usuarios}.
 * El rol se expone como autoridad "ROLE_GERENTE", "ROLE_MESERO", etc.
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) {
        UsuarioEntity usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con correo " + correo));
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .roles(usuario.getRol().name())
                .build();
    }
}
