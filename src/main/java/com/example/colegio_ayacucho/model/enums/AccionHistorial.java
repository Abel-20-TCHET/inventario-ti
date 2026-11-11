package com.example.colegio_ayacucho.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Acciones auditables sobre un activo.
 */
@Getter
@RequiredArgsConstructor
public enum AccionHistorial {

    CREACION("Creación", "plus-circle", "success"),
    EDICION("Edición", "pencil-square", "info"),
    ELIMINACION("Eliminación", "trash", "danger");

    private final String etiqueta;
    private final String icono;
    private final String claseCss;
}
