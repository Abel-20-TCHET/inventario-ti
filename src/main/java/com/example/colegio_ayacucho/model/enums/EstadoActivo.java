package com.example.colegio_ayacucho.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estado operativo del activo dentro de la CMDB.
 * La clase CSS se usa para pintar el badge en las vistas de Thymeleaf.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoActivo {

    OPERATIVO("Operativo", "success"),
    EN_REPARACION("En reparación", "warning"),
    DE_BAJA("De baja", "secondary");

    private final String etiqueta;
    private final String claseCss;
}
