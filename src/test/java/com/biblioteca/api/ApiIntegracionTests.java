package com.biblioteca.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pruebas-${random.uuid}")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiIntegracionTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String token() throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@biblioteca.com\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(respuesta).get("token").asText();
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/libros")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginIncorrectoResponde401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@biblioteca.com\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Correo o contraseña incorrectos."));
    }

    @Test
    void registroDevuelveToken() throws Exception {
        mvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Axel\",\"correo\":\"axel@correo.com\",\"password\":\"secreto123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void librosConAutoresSinRecursion() throws Exception {
        mvc.perform(get("/api/libros").header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").exists())
                .andExpect(jsonPath("$[0].autores[0].nombre").exists())
                .andExpect(jsonPath("$[0].autores[0].libros").doesNotExist());
    }

    @Test
    void crudDeLibroYRelacionMuchosAMuchos() throws Exception {
        String auth = token();

        String creado = mvc.perform(post("/api/libros").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"El llano en llamas\",\"editorialId\":3,\"autorIds\":[2]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.editorial.nombre").value("Fondo de Cultura Económica"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(creado).get("id").asLong();

        mvc.perform(post("/api/libros/" + id + "/autores/1").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autores.length()").value(2));

        mvc.perform(delete("/api/libros/" + id).header("Authorization", auth))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/libros/" + id).header("Authorization", auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void prestamoDevolucionYDisponibilidad() throws Exception {
        String auth = token();

        String creado = mvc.perform(post("/api/prestamos").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libroId\":1,\"socioId\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.socio.nombre").exists())
                .andExpect(jsonPath("$.fechaLimite").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode prestamo = json.readTree(creado);

        // RF04: el mismo libro no se puede prestar dos veces
        mvc.perform(post("/api/prestamos").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libroId\":1,\"socioId\":1}"))
                .andExpect(status().isConflict());

        // RF05: devolución
        mvc.perform(put("/api/prestamos/" + prestamo.get("id").asLong() + "/devolver").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaDevolucion").exists());

        // Ya devuelto, el libro se puede prestar otra vez
        mvc.perform(post("/api/prestamos").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libroId\":1,\"socioId\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void swaggerEsPublico() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
