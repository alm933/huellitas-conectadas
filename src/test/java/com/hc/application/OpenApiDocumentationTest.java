package com.hc.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:openapi;MODE=MySQL;DB_CLOSE_DELAY=-1",
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
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void documentationDescribesJwtPublicOperationsAndMultipart() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Huellitas Conectadas API"))
                .andExpect(jsonPath("$.servers[0].url").value("/"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/fotos/{id}'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/animales/publicar'].post.requestBody"
                        + ".content['multipart/form-data'].encoding.animal.contentType")
                        .value("application/json"))
                .andExpect(jsonPath("$.components.schemas.AnimalRequest.properties.tamanio").exists());
    }

    @Test
    void swaggerUiAndConfigurationAreAccessibleWithoutLogin() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
    }

    @Test
    void apiStillRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/animales")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/animales/publicar")).andExpect(status().isUnauthorized());
    }
}
