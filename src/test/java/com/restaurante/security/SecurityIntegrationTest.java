package com.restaurante.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de seguridad de extremo a extremo (Semana 10): confirman que la cadena de filtros
 * de Spring Security y las reglas de autorizacion por rol responden como se espera.
 * Necesitan PostgreSQL y MongoDB corriendo (ver docker-compose.yml) porque levantan el
 * contexto completo de la aplicacion.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PLATO_VALIDO = "{\"nombre\":\"Plato de prueba\",\"precio\":15000,"
            + "\"categoria\":\"Entrada\"}";

    @Test
    void crearPlato_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/platos")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLATO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPlato_conTokenInvalido_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/platos")
                        .header("Authorization", "Bearer tokenbasura")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLATO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "mesero@sushicraft.com", roles = "MESERO")
    void crearPlato_conRolMesero_devuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLATO_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gerente@sushicraft.com", roles = "GERENTE")
    void crearPlato_conRolGerente_devuelve201() throws Exception {
        // Nombre unico para que la prueba sea repetible sin chocar con una entrega anterior
        // (PlatoServiceImpl rechaza nombres duplicados con 409).
        String nombreUnico = "Plato seguridad test " + System.currentTimeMillis();
        mockMvc.perform(post("/api/v1/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + nombreUnico + "\",\"precio\":22000,"
                                + "\"categoria\":\"Entrada\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void login_conCredencialesInvalidas_devuelve401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gerente@sushicraft.com\",\"password\":\"claveIncorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conCredencialesValidas_devuelve200ConToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gerente@sushicraft.com\",\"password\":\"sushicraft123\"}"))
                .andExpect(status().isOk());
    }
}
