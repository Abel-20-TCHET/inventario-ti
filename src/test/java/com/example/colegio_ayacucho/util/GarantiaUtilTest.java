package com.example.colegio_ayacucho.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pruebas unitarias de la regla de garantías (sin Spring, JUnit puro).
 */
class GarantiaUtilTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);

    @Test
    @DisplayName("Una garantía que terminó ayer está VENCIDA")
    void garantiaVencida() {
        assertEquals(EstadoGarantia.VENCIDA,
                GarantiaUtil.estado(HOY.minusDays(1), HOY));
    }

    @Test
    @DisplayName("Una garantía que vence justo hoy se considera POR_VENCER")
    void garantiaVenceHoy() {
        assertEquals(EstadoGarantia.POR_VENCER,
                GarantiaUtil.estado(HOY, HOY));
    }

    @Test
    @DisplayName("Una garantía que vence en 60 días sigue POR_VENCER (límite inclusive)")
    void garantiaLimiteSesentaDias() {
        assertEquals(EstadoGarantia.POR_VENCER,
                GarantiaUtil.estado(HOY.plusDays(60), HOY));
    }

    @Test
    @DisplayName("Una garantía que vence en 61 días ya está VIGENTE")
    void garantiaVigente() {
        assertEquals(EstadoGarantia.VIGENTE,
                GarantiaUtil.estado(HOY.plusDays(61), HOY));
    }

    @Test
    @DisplayName("Sin fecha de fin de garantía se informa SIN_GARANTIA")
    void sinGarantia() {
        assertEquals(EstadoGarantia.SIN_GARANTIA, GarantiaUtil.estado(null, HOY));
        assertNull(GarantiaUtil.diasRestantes(null, HOY));
    }

    @Test
    @DisplayName("Los días restantes son negativos cuando la garantía venció")
    void diasRestantesNegativos() {
        assertEquals(-15L, GarantiaUtil.diasRestantes(HOY.minusDays(15), HOY));
    }
}
