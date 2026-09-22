package com.restaurante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Sushi Craft - API Restaurante",
                version = "1.0.0",
                description = "API REST para el restaurante Sushi Craft (app Kaze & Nori). "
                        + "Menu, mesas, pedidos, cuentas, reservas y parqueadero. "
                        + "Sin persistencia: los datos viven en memoria mientras la app esta corriendo.",
                contact = @Contact(name = "Kevin Angel", email = "kevin.angel-a@mail.escuelaing.edu.co")
        )
)
public class OpenApiConfig {

    @Bean
    public OpenAPI restauranteOpenAPI() {
        return new OpenAPI();
    }
}
