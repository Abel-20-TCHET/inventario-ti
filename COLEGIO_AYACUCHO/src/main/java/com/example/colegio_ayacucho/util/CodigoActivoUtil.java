package com.example.colegio_ayacucho.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidades para el código institucional de activos (CI-001, CI-002, ...).
 */
public final class CodigoActivoUtil {

    public static final String PATRON_REGEX = "^CI-[0-9]{3,}$";
    private static final Pattern PATRON = Pattern.compile("^CI-(\\d+)$");

    private CodigoActivoUtil() {
    }

    /** Devuelve CI-001, CI-042, ... a partir del número correlativo. */
    public static String formatear(long numero) {
        return String.format("CI-%03d", numero);
    }

    /** Extrae el número de un código válido; devuelve 0 si no se puede. */
    public static long numero(String codigo) {
        if (codigo == null) {
            return 0L;
        }
        Matcher m = PATRON.matcher(codigo.trim().toUpperCase());
        return m.matches() ? Long.parseLong(m.group(1)) : 0L;
    }

    /** Sugiere el siguiente código a partir del número máximo existente. */
    public static String siguiente(Integer maxNumero) {
        return formatear(maxNumero == null ? 1L : maxNumero + 1L);
    }
}
