package com.biblioteca.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Configuración de Swagger UI: http://localhost:8080/swagger-ui/index.html
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API")
                        .description("API REST para administrar libros, socios, préstamos, editoriales y autores. "
                                + "Primero inicia sesión en /api/auth/login, copia el token y pégalo en Authorize.")
                        .version("1.0"))
                // Servidor relativo para que "Try it out" funcione también en Codespaces
                .servers(List.of(new Server().url("/")))
                // Esquema bearerAuth: habilita el botón Authorize para mandar el JWT
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
