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
    @Autowired private com.hc.application.repository.UsuarioRepository usuarios;
    @Autowired private com.hc.application.repository.OrganizacionRepository organizaciones;
    @Autowired private com.hc.application.repository.AnimalRepository animales;
    @Autowired private com.hc.application.security.JwtService jwt;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private com.hc.application.entity.UsuarioEntity usuario(com.hc.application.enums.UsuarioRol rol) {
        return usuarios.saveAndFlush(com.hc.application.entity.UsuarioEntity.builder()
                .name("Ana").lastName("Prueba").email(java.util.UUID.randomUUID() + "@example.com")
                .pass("test-hash").role(rol).build());
    }

    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwords;

    @Test
    @org.springframework.transaction.annotation.Transactional
    void browserSessionKeepsJwtInCookieAndProtectsCookieAuthenticatedWrites() throws Exception {
        var user = usuario(com.hc.application.enums.UsuarioRol.ADOPTANTE);
        user.setPass(passwords.encode("test-password"));
        usuarios.saveAndFlush(user);
        String auth = "/api/v1/auth";
        String browserHeader = "X-Huellitas-Request";
        String cookieName = "__Host-huellitas-session";
        String payload = "{\"email\":\"" + user.getEmail() + "\",\"pass\":\"test-password\"}";

        mvc.perform(post(auth + "/login/browser").contentType("application/json").content(payload))
                .andExpect(status().isBadRequest());
        var login = mvc.perform(post(auth + "/login/browser")
                .header("Origin", "https://hc.itechk.us").header(browserHeader, "1")
                .contentType("application/json").content(payload))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.role").value("ADOPTANTE"))
                .andReturn().getResponse();
        String setCookie = login.getHeader("Set-Cookie");
        org.junit.jupiter.api.Assertions.assertNotNull(setCookie);
        org.junit.jupiter.api.Assertions.assertTrue(setCookie.contains("HttpOnly")
                && setCookie.contains("Secure") && setCookie.contains("SameSite=Lax"));
        String token = setCookie.substring(cookieName.length() + 1, setCookie.indexOf(';'));
        var cookie = new jakarta.servlet.http.Cookie(cookieName, token);

        mvc.perform(get(auth + "/session")).andExpect(status().isUnauthorized());
        mvc.perform(get(auth + "/session").cookie(cookie))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(user.getEmail()));
        mvc.perform(get("/api/v1/animales").cookie(cookie)).andExpect(status().isOk());
        mvc.perform(post(auth + "/logout").cookie(cookie)).andExpect(status().isForbidden());
        mvc.perform(post(auth + "/logout").cookie(cookie).header(browserHeader, "1")
                .header("Origin", "https://example.org")).andExpect(status().isForbidden());
        mvc.perform(post(auth + "/logout").cookie(cookie).header(browserHeader, "1"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void adoptionLifecycleUsesJwtPreventsDuplicatesAndRemovesPetFromAvailability() throws Exception {
        var owner = usuario(com.hc.application.enums.UsuarioRol.ORGANIZACION);
        var adopter = usuario(com.hc.application.enums.UsuarioRol.ADOPTANTE);
        var other = usuario(com.hc.application.enums.UsuarioRol.ADOPTANTE);
        var shelter = new com.hc.application.entity.OrganizacionEntity();
        shelter.setUsuario(owner); shelter.setNombre("Refugio");
        shelter.setTipo(com.hc.application.enums.TipoOrganizacion.REFUGIO);
        shelter.setCorreo(java.util.UUID.randomUUID() + "@example.com");
        shelter.setTelefono("123456789"); shelter.setDireccion("Calle 1"); shelter.setDistrito("Centro");
        organizaciones.saveAndFlush(shelter);
        var animal = new com.hc.application.entity.AnimalEntity();
        animal.setNombre("Luna"); animal.setEspecie("Gato"); animal.setEdadMeses(12);
        animal.setSexo(com.hc.application.enums.SexoAnimal.HEMBRA);
        animal.setTamano(com.hc.application.enums.TamanoAnimal.MEDIANO);
        animal.setDescripcion("Busca hogar"); animal.setOrganizacion(shelter);
        animales.saveAndFlush(animal);
        String path = "/api/v1/solicitudes-adopcion";
        String token = "Bearer " + jwt.generarToken(adopter);
        String catalogo = "/api/v1/animales/catalogo";
        mvc.perform(get(catalogo)).andExpect(status().isUnauthorized());
        mvc.perform(get(catalogo).header("Authorization", token)
                .param("size", "1").param("especie", "GATO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Luna"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(catalogo).header("Authorization", token)
                .param("page", "1").param("size", "1").param("especie", "GATO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(get(catalogo).header("Authorization", token).param("especie", "PERRO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get(catalogo).header("Authorization", token).param("size", "51"))
                .andExpect(status().isBadRequest());
        String payload = "{\"mensaje\":\"\",\"tieneOtrasMascotas\":false,\"tipoVivienda\":\"CASA\"}";
        mvc.perform(post(path + "/animales/" + animal.getId())).andExpect(status().isUnauthorized());
        mvc.perform(post(path + "/animales/" + animal.getId())
                .header("Authorization", "Bearer " + jwt.generarToken(owner))).andExpect(status().isForbidden());
        mvc.perform(post(path + "/animales/" + animal.getId()).header("Authorization", token)
                .contentType("application/json").content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("PENDIENTE"));
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.FALSE,
                jdbc.queryForObject("select tiene_otras_mascotas from solicitudes_adopcion where animal_id = ? and adoptante_id = ?",
                        Boolean.class, animal.getId(), adopter.getId()));
        org.junit.jupiter.api.Assertions.assertEquals("CASA",
                jdbc.queryForObject("select tipo_vivienda from solicitudes_adopcion where animal_id = ? and adoptante_id = ?",
                        String.class, animal.getId(), adopter.getId()));
        org.junit.jupiter.api.Assertions.assertEquals("Me interesa adoptar esta mascota.",
                jdbc.queryForObject("select motivo from solicitudes_adopcion where animal_id = ? and adoptante_id = ?",
                        String.class, animal.getId(), adopter.getId()));
        mvc.perform(post(path + "/animales/" + animal.getId()).header("Authorization", token)
                .contentType("application/json").content(payload))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").isNotEmpty());
        mvc.perform(post(path + "/animales/" + animal.getId())
                .header("Authorization", "Bearer " + jwt.generarToken(other))
                .contentType("application/json").content("{\"tieneOtrasMascotas\":true,\"tipoVivienda\":\"DEPARTAMENTO\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get(path + "/mias").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        var repo = solicitudRepository;
        var requests = repo.findByAnimal_Organizacion_IdOrderByFechaSolicitudDesc(shelter.getId());
        Long id = requests.stream().filter(s -> s.getAdoptante().getId().equals(adopter.getId())).findFirst().orElseThrow().getId();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(path + "/" + id + "/estado")
                .header("Authorization", token).contentType("application/json").content("{\"estado\":\"APROBADA\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(path + "/" + id + "/estado")
                .header("Authorization", "Bearer " + jwt.generarToken(owner))
                .contentType("application/json").content("{\"estado\":\"APROBADA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("APROBADA"));
        org.junit.jupiter.api.Assertions.assertEquals(com.hc.application.enums.EstadoAnimal.ADOPTADO, animales.findById(animal.getId()).orElseThrow().getEstado());
        org.junit.jupiter.api.Assertions.assertEquals(1, repo.findByAnimal_IdAndEstado(animal.getId(), com.hc.application.enums.EstadoSolicitudAdopcion.RECHAZADA).size());
        mvc.perform(post(path + "/animales/" + animal.getId()).header("Authorization", token)
                .contentType("application/json").content(payload))
                .andExpect(status().isConflict());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/animales/" + animal.getId())
                .header("Authorization", "Bearer " + jwt.generarToken(owner))).andExpect(status().isNoContent());
        animales.flush();
        org.junit.jupiter.api.Assertions.assertTrue(animales.findById(animal.getId()).isEmpty());
    }

    @Autowired private com.hc.application.repository.SolicitudAdopcionRepository solicitudRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    void adoptionRequiresHousingAndAnExplicitPetAnswer() throws Exception {
        var adopter = usuario(com.hc.application.enums.UsuarioRol.ADOPTANTE);
        String token = "Bearer " + jwt.generarToken(adopter);
        for (String invalid : java.util.List.of("{}", "{\"tipoVivienda\":\"CASA\"}",
                "{\"tieneOtrasMascotas\":false,\"tipoVivienda\":\"   \"}")) {
            mvc.perform(post("/api/v1/solicitudes-adopcion/animales/2")
                    .header("Authorization", token).contentType("application/json").content(invalid))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void internalErrorDispatchIsNotReplacedWithUnauthorized() throws Exception {
        mvc.perform(get("/error").with(request -> {
                    request.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                    return request;
                })
                .requestAttr(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE, 500)
                .requestAttr(jakarta.servlet.RequestDispatcher.ERROR_REQUEST_URI,
                        "/api/v1/solicitudes-adopcion/animales/2"))
                .andExpect(status().isInternalServerError());
        // A normal request to the same path still requires authentication.
        mvc.perform(get("/error")).andExpect(status().isUnauthorized());
    }

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
