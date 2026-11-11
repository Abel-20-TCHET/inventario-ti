package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.TipoActivo;
import com.example.colegio_ayacucho.model.Ubicacion;
import com.example.colegio_ayacucho.model.enums.Criticidad;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la exportación CSV: BOM UTF-8, separador ";" y entrecomillado.
 */
class CsvServiceTest {

    private final CsvService csvService = new CsvService();

    @Test
    @DisplayName("El CSV inicia con BOM UTF-8 y contiene la cabecera en español")
    void incluyeBomYCabecera() {
        byte[] bytes = csvService.exportarActivos(List.of(activoSimple()));
        String texto = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(texto.startsWith("\uFEFF"), "Debe iniciar con el BOM UTF-8 para Excel");
        assertTrue(texto.contains("Código;Nombre;Tipo;Marca;Modelo;N° de serie;Ubicación"),
                "La cabecera debe estar en español y usar ';'");
        assertTrue(texto.contains("CI-001"));
    }

    @Test
    @DisplayName("Los campos con punto y coma o comillas se entrecomillan correctamente")
    void escapaCaracteresEspeciales() {
        Activo activo = activoSimple();
        activo.setNombre("Switch \"core\"; principal");

        String texto = new String(csvService.exportarActivos(List.of(activo)),
                StandardCharsets.UTF_8);

        assertTrue(texto.contains("\"Switch \"\"core\"\"; principal\""),
                "Las comillas deben duplicarse y el campo ir entre comillas");
    }

    @Test
    @DisplayName("Un activo sin garantía se exporta como 'Sin garantía'")
    void exportaEstadoDeGarantia() {
        Activo activo = activoSimple();
        activo.setFechaFinGarantia(null);

        String texto = new String(csvService.exportarActivos(List.of(activo)),
                StandardCharsets.UTF_8);

        assertTrue(texto.contains("Sin garantía"));
        assertEquals(2, texto.split("\r\n").length, "Cabecera + 1 fila de datos");
    }

    private Activo activoSimple() {
        Activo a = new Activo();
        a.setCodigo("CI-001");
        a.setNombre("Firewall perimetral");
        a.setTipo(TipoActivo.builder().id(1L).nombre("Firewall").build());
        a.setUbicacion(Ubicacion.builder().id(1L).nombre("Gabinete principal").build());
        a.setMarca("Fortinet");
        a.setModelo("FortiGate 100F");
        a.setNumeroSerie("FGT-001");
        a.setEstado(EstadoActivo.OPERATIVO);
        a.setCriticidad(Criticidad.ALTA);
        a.setFechaCompra(LocalDate.of(2024, 1, 15));
        a.setFechaFinGarantia(LocalDate.of(2027, 1, 15));
        return a;
    }
}
