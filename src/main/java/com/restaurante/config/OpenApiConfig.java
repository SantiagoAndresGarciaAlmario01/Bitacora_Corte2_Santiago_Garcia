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

/**
 * Swagger con boton "Authorize": se hace login en /api/v1/auth/login, se copia
 * el token y se pega ahi para probar los endpoints protegidos.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Sushi Craft - API Restaurante",
                version = "2.0.0",
                description = "API REST para el restaurante Sushi Craft (app Kaze & Nori): menu, mesas, cuentas, "
                        + "pedidos, reservas y parqueadero. Datos en PostgreSQL, historial de pedidos en MongoDB "
                        + "y acceso protegido con JWT por roles.",
                contact = @Contact(name = "Santiago Garcia", email = "santiago.garcia-a@mail.escuelaing.edu.co")
        ),
        security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT)
)
@SecurityScheme(
        name = OpenApiConfig.ESQUEMA_JWT,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    public static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI restauranteOpenAPI() {
        return new OpenAPI();
    }
}
