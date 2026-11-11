package com.example.colegio_ayacucho.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Regla de negocio de garantías, centralizada y sin dependencias de Spring
 * para poder probarla con JUnit puro.
 *
 * Criterio:
 *   - Sin fecha de fin de garantía  -> SIN_GARANTIA
 *   - fechaFin < hoy                -> VENCIDA
 *   - hoy <= fechaFin <= hoy + 60   -> POR_VENCER
 *   - fechaFin > hoy + 60           -> VIGENTE
 */
public final class GarantiaUtil {

    /** Días de anticipación con los que se alerta el vencimiento. */
    public static final int DIAS_ALERTA = 60;

    private GarantiaUtil() {
    }

    public static EstadoGarantia estado(LocalDate fechaFinGarantia) {
        return estado(fechaFinGarantia, LocalDate.now());
    }

    public static EstadoGarantia estado(LocalDate fechaFinGarantia, LocalDate hoy) {
        if (fechaFinGarantia == null) {
            return EstadoGarantia.SIN_GARANTIA;
        }
        if (fechaFinGarantia.isBefore(hoy)) {
            return EstadoGarantia.VENCIDA;
        }
        if (!fechaFinGarantia.isAfter(hoy.plusDays(DIAS_ALERTA))) {
            return EstadoGarantia.POR_VENCER;
        }
        return EstadoGarantia.VIGENTE;
    }

    /** Días que faltan para el vencimiento (negativo si ya venció). */
    public static Long diasRestantes(LocalDate fechaFinGarantia, LocalDate hoy) {
        if (fechaFinGarantia == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(hoy, fechaFinGarantia);
    }

    /** Texto listo para mostrar en la interfaz. */
    public static String texto(LocalDate fechaFinGarantia) {
        if (fechaFinGarantia == null) {
            return "Sin garantía registrada";
        }
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaFinGarantia);
        if (dias < 0) {
            return "Vencida hace " + Math.abs(dias) + " días";
        }
        if (dias == 0) {
            return "Vence hoy";
        }
        if (dias <= DIAS_ALERTA) {
            return "Vence en " + dias + " días";
        }
        return "Vigente (" + dias + " días restantes)";
    }
}
