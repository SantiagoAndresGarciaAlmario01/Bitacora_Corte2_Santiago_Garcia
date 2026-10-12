package com.restaurante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Sushi Craft - API Restaurante",
                version = "1.0.0",
                description = "API REST para el restaurante Sushi Craft (app Kaze & Nori). "
                        + "Menu, mesas, pedidos, cuentas, reservas y parqueadero. "
                        + "Persistencia en PostgreSQL (JPA) y MongoDB (historial de pedidos), "
                        + "protegida con login y JWT: haz POST /auth/login y usa el boton "
                        + "Authorize con el token que te devuelve.",
                contact = @Contact(name = "Santiago Garcia", email = "santiago.garcia-a@mail.escuelaing.edu.co")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega aqui el token que devuelve POST /auth/login (sin la palabra 'Bearer', Swagger la agrega solo)."
)
public class OpenApiConfig {

    @Bean
    public OpenAPI restauranteOpenAPI() {
        return new OpenAPI();
    }
}
