package com.restaurante.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lee el header {@code Authorization: Bearer <token>} de cada peticion. Si el
 * token es valido, deja al usuario autenticado en el SecurityContext; si no
 * trae token o es invalido, no hace nada y Spring Security respondera 401
 * cuando la ruta lo requiera.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFiltroAutenticacion extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetallesService usuarioDetallesService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIJO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIJO.length());
        try {
            String correo = jwtService.extraerCorreo(token);
            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails usuario = usuarioDetallesService.loadUserByUsername(correo);
                if (jwtService.esValidoPara(token, usuario.getUsername())) {
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            log.warn("Token rechazado en {}: {}", request.getRequestURI(), e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
