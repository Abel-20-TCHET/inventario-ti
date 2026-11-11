package com.example.colegio_ayacucho.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Semáforo de garantía usado en listados y dashboard.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoGarantia {

    VENCIDA("Vencida", "danger"),
    POR_VENCER("Por vencer", "warning"),
    VIGENTE("Vigente", "success"),
    SIN_GARANTIA("Sin garantía", "secondary");

    private final String etiqueta;
    private final String claseCss;
}
