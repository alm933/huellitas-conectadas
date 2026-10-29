package com.hc.application.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI huellitasOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Huellitas Conectadas API")
                        .version("v1")
                        .description("API de adopción de mascotas. Inicia sesión en /api/v1/auth/login "
                                + "y pega solo el token JWT en Authorize. Las operaciones conservan "
                                + "los permisos de ADMIN, ORGANIZACION y ADOPTANTE."))
                // Relative origin works both on localhost and through the HTTPS tunnel.
                .servers(List.of(new Server().url("/").description("Servidor actual")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public OpenApiCustomizer publicOperationsAndPhotoUpload() {
        return api -> api.getPaths().forEach((path, item) -> {
            if (path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/fotos/")) {
                item.readOperations().forEach(operation -> operation.setSecurity(List.of()));
            }
            if (path.equals("/api/v1/animales/publicar") && item.getPost() != null) {
                var body = item.getPost().getRequestBody();
                if (body != null && body.getContent() != null) {
                    var multipart = body.getContent().get("multipart/form-data");
                    if (multipart != null) {
                        multipart.addEncoding("animal", new Encoding().contentType("application/json"));
                    }
                }
            }
        });
    }
}
