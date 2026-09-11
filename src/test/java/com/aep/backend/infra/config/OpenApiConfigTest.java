package com.aep.backend.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    @DisplayName("Deve configurar a documentação OpenAPI com esquema JWT")
    void deveConfigurarDocumentacaoOpenApiComEsquemaJwt() {
        OpenApiConfig openApiConfig = new OpenApiConfig();

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertEquals("API Sistema de Denúncias AEP", openAPI.getInfo().getTitle());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("bearerAuth"));
        assertFalse(openAPI.getSecurity().isEmpty());
    }
}
