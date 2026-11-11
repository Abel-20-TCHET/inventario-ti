package com.example.colegio_ayacucho;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de humo de la capa web: renderiza las plantillas reales con H2 y los
 * datos de ejemplo cargados (no requiere PostgreSQL).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.seed.enabled=true")
class WebSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("La pantalla de login se renderiza")
    void loginSeRenderiza() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inventario de Activos de TI")));
    }

    @Test
    @DisplayName("El panel exige autenticación")
    void panelRequiereLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("El listado de activos se renderiza con los datos de ejemplo")
    void listadoDeActivos() throws Exception {
        mockMvc.perform(get("/activos").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inventario de activos")))
                .andExpect(content().string(containsString("CI-001")))
                .andExpect(content().string(containsString("Exportar CSV")));
    }

    @Test
    @DisplayName("El TECNICO no ve el botón de eliminar")
    void tecnicoNoVeEliminar() throws Exception {
        mockMvc.perform(get("/activos").with(user("tecnico1").roles("TECNICO")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("aria-label=\"Eliminar\""))));
    }

    @Test
    @DisplayName("El TECNICO recibe 403 en la gestión de usuarios")
    void tecnicoNoAccedeAUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios").with(user("tecnico1").roles("TECNICO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El detalle del activo muestra la pestaña Historial")
    void detalleDeActivo() throws Exception {
        mockMvc.perform(get("/activos/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Historial")));
    }

    @Test
    @DisplayName("El formulario de alta se renderiza con el siguiente código sugerido")
    void formularioNuevoActivo() throws Exception {
        mockMvc.perform(get("/activos/nuevo").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrar activo")))
                .andExpect(content().string(containsString("name=\"codigo\"")))
                .andExpect(content().string(containsString("value=\"CI-")));
    }

    @Test
    @DisplayName("Crear un activo con CSRF redirige al listado")
    void crearActivo() throws Exception {
        mockMvc.perform(post("/activos")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("codigo", "CI-900")
                        .param("nombre", "Equipo de prueba web")
                        .param("tipoId", "1")
                        .param("ubicacionId", "1")
                        .param("estado", "OPERATIVO")
                        .param("criticidad", "ALTA")
                        .param("fechaCompra", "2026-01-10")
                        .param("fechaFinGarantia", "2028-01-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activos"));
    }

    @Test
    @DisplayName("Un POST sin token CSRF se rechaza")
    void postSinCsrfSeRechaza() throws Exception {
        mockMvc.perform(post("/activos")
                        .with(user("admin").roles("ADMIN"))
                        .param("codigo", "CI-901")
                        .param("nombre", "Sin CSRF")
                        .param("tipoId", "1")
                        .param("ubicacionId", "1")
                        .param("estado", "OPERATIVO")
                        .param("criticidad", "ALTA"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("La exportación CSV devuelve un archivo con BOM UTF-8")
    void exportarCsv() throws Exception {
        mockMvc.perform(get("/activos/exportar").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".csv")))
                .andExpect(content().string(startsWith("\uFEFF")));
    }

    @Test
    @DisplayName("El dashboard muestra los indicadores y las secciones nuevas")
    void dashboardMuestraIndicadores() throws Exception {
        mockMvc.perform(get("/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Total de equipos")))
                .andExpect(content().string(containsString("Garantías vencidas")))
                .andExpect(content().string(containsString("Salud de las garantías")))
                .andExpect(content().string(containsString("Actividad reciente")))
                .andExpect(content().string(containsString("Equipos por tipo")));
    }

    @Test
    @DisplayName("La gestión de tipos se renderiza para el ADMIN")
    void tiposParaAdmin() throws Exception {
        mockMvc.perform(get("/tipos").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Punto de acceso")));
    }
}
