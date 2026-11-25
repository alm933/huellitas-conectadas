package com.hc.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:public-catalog;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration=3600000",
        "GARAGE_S3_ENDPOINT=http://127.0.0.1:3900", "GARAGE_S3_REGION=garage",
        "GARAGE_S3_BUCKET=test", "GARAGE_S3_ACCESS_KEY_ID=test",
        "GARAGE_S3_SECRET_KEY=test"
})
@AutoConfigureMockMvc
class PublicCatalogSecurityTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void publicPreviewIsReadableButExistingCatalogAndAnimalListStillRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/animales/publicos/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(6));

        mvc.perform(get("/api/v1/animales/publicos/catalogo").param("size", "51"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/v1/animales/catalogo"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/animales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void preflightAllowsTheNewFrontendWithCredentialedRequests() throws Exception {
        mvc.perform(options("/api/v1/auth/session")
                        .header("Origin", "https://huellitasconectas.online")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin",
                        "https://huellitasconectas.online"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void preflightRejectsUnregisteredOrigins() throws Exception {
        mvc.perform(options("/api/v1/auth/session")
                        .header("Origin", "https://sitio-no-autorizado.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
