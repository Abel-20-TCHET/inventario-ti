package com.example.colegio_ayacucho.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Roles del sistema. ADMIN gestiona todo; TECNICO solo consulta y edita activos.
 */
@Getter
@RequiredArgsConstructor
public enum Rol {

    ADMIN("Administrador"),
    TECNICO("Técnico");

    private final String etiqueta;
}
