package com.example.colegio_ayacucho.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Criticidad del activo para priorizar la gestión de incidentes (ITIL).
 */
@Getter
@RequiredArgsConstructor
public enum Criticidad {

    ALTA("Alta", "danger"),
    MEDIA("Media", "warning"),
    BAJA("Baja", "info");

    private final String etiqueta;
    private final String claseCss;
}
