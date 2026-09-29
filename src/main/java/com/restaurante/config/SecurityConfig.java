package com.restaurante.config;

import static com.restaurante.model.domain.Rol.CLIENTE;
import static com.restaurante.model.domain.Rol.COCINERO;
import static com.restaurante.model.domain.Rol.GERENTE;
import static com.restaurante.model.domain.Rol.MESERO;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.restaurante.model.domain.Rol;
import com.restaurante.security.JwtFiltroAutenticacion;
import com.restaurante.security.RespuestasSeguridad;
import com.restaurante.security.UsuarioDetallesService;

import lombok.RequiredArgsConstructor;

/**
 * Reglas de acceso por rol. Resumen (ver tabla completa en el README):
 * <ul>
 *   <li>Publico: login/registro, Swagger y el menu.</li>
 *   <li>GERENTE: administra platos y mesas, y puede todo lo demas.</li>
 *   <li>MESERO: cuentas, pedidos, reservas y parqueadero.</li>
 *   <li>COCINERO: ve pedidos y los mueve de estado.</li>
 *   <li>CLIENTE: crea, reprograma y cancela reservas.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String PLATOS = "/api/v1/platos/**";
    private static final String MESAS = "/api/v1/mesas/**";
    private static final String PEDIDOS = "/api/v1/pedidos/**";
    private static final String RESERVAS = "/api/v1/reservas/**";

    private final JwtFiltroAutenticacion jwtFiltro;
    private final UsuarioDetallesService usuarioDetallesService;
    private final RespuestasSeguridad respuestasSeguridad;

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publico
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/menu/**").permitAll()

                        // Platos: el personal consulta, solo el gerente modifica
                        .requestMatchers(HttpMethod.GET, PLATOS).hasAnyRole(roles(GERENTE, MESERO, COCINERO))
                        .requestMatchers(PLATOS).hasRole(GERENTE.name())

                        // Mesas: meseros consultan y cambian estado; gerente administra
                        .requestMatchers(HttpMethod.GET, MESAS).hasAnyRole(roles(GERENTE, MESERO))
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/mesas/*/estado").hasAnyRole(roles(GERENTE, MESERO))
                        .requestMatchers(MESAS).hasRole(GERENTE.name())

                        // Pedidos: cocina ve y mueve estados; meseros toman pedidos; gerente elimina
                        .requestMatchers(HttpMethod.GET, PEDIDOS).hasAnyRole(roles(GERENTE, MESERO, COCINERO))
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/estado").hasAnyRole(roles(GERENTE, MESERO, COCINERO))
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/pedidos/*").hasRole(GERENTE.name())
                        .requestMatchers(PEDIDOS).hasAnyRole(roles(GERENTE, MESERO))

                        // Cuentas y parqueadero: personal de sala
                        .requestMatchers("/api/v1/cuentas/**", "/api/v1/parqueadero/**").hasAnyRole(roles(GERENTE, MESERO))

                        // Reservas: el cliente reserva y gestiona; el personal consulta; el gerente elimina
                        .requestMatchers(HttpMethod.GET, RESERVAS).hasAnyRole(roles(GERENTE, MESERO))
                        .requestMatchers(HttpMethod.DELETE, RESERVAS).hasRole(GERENTE.name())
                        .requestMatchers(RESERVAS).hasAnyRole(roles(GERENTE, MESERO, CLIENTE))

                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respuestasSeguridad)
                        .accessDeniedHandler(respuestasSeguridad))
                .authenticationProvider(proveedorAutenticacion())
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider proveedorAutenticacion() {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider();
        proveedor.setUserDetailsService(usuarioDetallesService);
        proveedor.setPasswordEncoder(passwordEncoder());
        return proveedor;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    /**
     * Como el filtro JWT es un @Component, Spring Boot lo registraria tambien
     * como filtro general del servidor. Se desactiva ese registro para que
     * solo corra dentro de la cadena de Spring Security.
     */
    @Bean
    public FilterRegistrationBean<JwtFiltroAutenticacion> desactivarRegistroAutomaticoJwt(JwtFiltroAutenticacion filtro) {
        FilterRegistrationBean<JwtFiltroAutenticacion> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    private static String[] roles(Rol... roles) {
        return Arrays.stream(roles).map(Rol::name).toArray(String[]::new);
    }
}
